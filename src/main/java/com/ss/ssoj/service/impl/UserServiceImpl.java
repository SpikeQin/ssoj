package com.ss.ssoj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ss.ssoj.auth.UserContext;
import com.ss.ssoj.auth.JwtUtil;
import com.ss.ssoj.common.BusinessException;
import com.ss.ssoj.common.PageResult;
import com.ss.ssoj.dto.ChangePasswordRequest;
import com.ss.ssoj.dto.LoginRequest;
import com.ss.ssoj.dto.ProfileRequest;
import com.ss.ssoj.dto.RegisterRequest;
import com.ss.ssoj.dto.UserCreateRequest;
import com.ss.ssoj.entity.SysConfig;
import com.ss.ssoj.entity.SysUser;
import com.ss.ssoj.mapper.SysConfigMapper;
import com.ss.ssoj.mapper.SysUserMapper;
import com.ss.ssoj.mapper.StatsMapper;
import com.ss.ssoj.service.UserService;
import com.ss.ssoj.vo.RankRowVO;
import com.ss.ssoj.vo.UserAdminVO;
import com.ss.ssoj.vo.LoginVO;
import com.ss.ssoj.vo.UserVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    private static final int MAX_FAIL_COUNT = 5;
    private static final int LOCK_MINUTES = 10;
    /** 重置后的初始密码 */
    private static final String INITIAL_PASSWORD = "123456";

    private final SysUserMapper userMapper;
    private final SysConfigMapper configMapper;
    private final StatsMapper statsMapper;
    private final JwtUtil jwtUtil;
    public UserServiceImpl(SysUserMapper userMapper, SysConfigMapper configMapper,
                           StatsMapper statsMapper, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.configMapper = configMapper;
        this.statsMapper = statsMapper;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void register(RegisterRequest request) {
        if ("0".equals(readConfig("register_enabled"))) {
            throw new BusinessException("当前未开放注册，请联系管理员");
        }
        if (userMapper.countByUsernameIncludeDeleted(request.getUsername()) > 0) {
            throw new BusinessException("该用户名已被注册");
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setNickname(StringUtils.hasText(request.getNickname())
                ? request.getNickname() : request.getUsername());
        user.setEmail(request.getEmail());
        user.setRole(0);
        user.setStatus(1);
        user.setLoginFailCount(0);
        userMapper.insert(user);
    }

    @Override
    public LoginVO login(LoginRequest request) {
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername()));
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (Integer.valueOf(0).equals(user.getStatus())) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }

        LocalDateTime now = LocalDateTime.now();
        if (user.getLockUntil() != null && user.getLockUntil().isAfter(now)) {
            long minutes = Duration.between(now, user.getLockUntil()).toMinutes() + 1;
            throw new BusinessException("账号已锁定，请 " + minutes + " 分钟后重试");
        }

        if (!passwordMatches(request.getPassword(), user.getPassword())) {
            int failCount = (user.getLoginFailCount() == null ? 0 : user.getLoginFailCount()) + 1;
            if (failCount >= MAX_FAIL_COUNT) {
                userMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                        .eq(SysUser::getId, user.getId())
                        .set(SysUser::getLoginFailCount, 0)
                        .set(SysUser::getLockUntil, now.plusMinutes(LOCK_MINUTES)));
                throw new BusinessException("密码错误次数过多，账号已锁定 " + LOCK_MINUTES + " 分钟");
            }
            userMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                    .eq(SysUser::getId, user.getId())
                    .set(SysUser::getLoginFailCount, failCount));
            throw new BusinessException("用户名或密码错误，还可尝试 " + (MAX_FAIL_COUNT - failCount) + " 次");
        }

        userMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, user.getId())
                .set(SysUser::getLoginFailCount, 0)
                .set(SysUser::getLockUntil, null)
                .set(SysUser::getLastLoginTime, now));

        String token = jwtUtil.generate(user.getId(), user.getUsername(), user.getRole());
        return new LoginVO(token, UserVO.from(user));
    }

    @Override
    public UserVO getProfile(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "账号不存在");
        }
        return UserVO.from(user);
    }

    @Override
    public UserVO updateProfile(Long userId, ProfileRequest request) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "账号不存在");
        }
        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());
        userMapper.updateById(user);
        return UserVO.from(user);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "账号不存在");
        }
        if (!passwordMatches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码不正确");
        }
        if (passwordMatches(request.getNewPassword(), user.getPassword())) {
            throw new BusinessException("新密码不能与原密码相同");
        }
        userMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, userId)
                .set(SysUser::getPassword, request.getNewPassword()));
    }

    /** 密码以明文保存，直接比较即可 */
    private boolean passwordMatches(String raw, String stored) {
        return raw != null && raw.equals(stored);
    }

    private String readConfig(String key) {
        SysConfig config = configMapper.selectOne(
                new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, key));
        return config == null ? null : config.getConfigValue();
    }

    @Override
    public PageResult<UserAdminVO> pageUsers(String keyword, int page, int size) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String value = keyword.trim();
            wrapper.and(w -> w.like(SysUser::getUsername, value).or().like(SysUser::getNickname, value));
        }
        wrapper.orderByAsc(SysUser::getId);

        Page<SysUser> result = userMapper.selectPage(new Page<>(Math.max(page, 1), Math.max(size, 1)), wrapper);
        // 一次查询拿到全部用户的提交统计，避免逐行查库
        Map<Long, RankRowVO> statMap = new HashMap<>();
        for (RankRowVO stat : statsMapper.selectUserStats()) {
            statMap.put(stat.getUserId(), stat);
        }

        List<UserAdminVO> records = new ArrayList<>();
        for (SysUser user : result.getRecords()) {
            RankRowVO stat = statMap.get(user.getId());
            records.add(UserAdminVO.from(user,
                    stat == null || stat.getSubmitCount() == null ? 0 : stat.getSubmitCount(),
                    stat == null || stat.getAcceptCount() == null ? 0 : stat.getAcceptCount()));
        }
        return new PageResult<>(records, result.getTotal());
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "账号不存在");
        }
        if (id.equals(UserContext.userId())) {
            throw new BusinessException("不能修改自己的账号状态");
        }
        userMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, id)
                .set(SysUser::getStatus, Integer.valueOf(0).equals(status) ? 0 : 1));
    }

    @Override
    public void resetPassword(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "账号不存在");
        }
        userMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, id)
                .set(SysUser::getPassword, INITIAL_PASSWORD)
                .set(SysUser::getLoginFailCount, 0)
                .set(SysUser::getLockUntil, null));
    }

    @Override
    public UserAdminVO createUser(UserCreateRequest request) {
        if (userMapper.countByUsernameIncludeDeleted(request.getUsername()) > 0) {
            throw new BusinessException("该用户名已存在，已被删除的账号仍占用用户名");
        }
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setNickname(request.getNickname());
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());
        user.setRole(Integer.valueOf(1).equals(request.getRole()) ? 1 : 0);
        user.setStatus(1);
        user.setLoginFailCount(0);
        userMapper.insert(user);
        return UserAdminVO.from(user, 0, 0);
    }

    @Override
    public void deleteUser(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "账号不存在");
        }
        if (id.equals(UserContext.userId())) {
            throw new BusinessException("不能删除自己的账号");
        }
        // 逻辑删除，历史提交记录继续保留
        userMapper.deleteById(id);
    }
}
