package com.ss.ssoj.service;

import com.ss.ssoj.dto.ChangePasswordRequest;
import com.ss.ssoj.dto.LoginRequest;
import com.ss.ssoj.dto.ProfileRequest;
import com.ss.ssoj.dto.RegisterRequest;
import com.ss.ssoj.dto.UserCreateRequest;
import com.ss.ssoj.common.PageResult;
import com.ss.ssoj.vo.UserAdminVO;
import com.ss.ssoj.vo.LoginVO;
import com.ss.ssoj.vo.UserVO;

public interface UserService {

    /** 注册新账号，默认学生角色 */
    void register(RegisterRequest request);

    /** 登录并签发令牌 */
    LoginVO login(LoginRequest request);

    /** 查询个人资料 */
    UserVO getProfile(Long userId);

    /** 修改昵称与邮箱 */
    UserVO updateProfile(Long userId, ProfileRequest request);

    /** 修改密码，需要校验原密码 */
    void changePassword(Long userId, ChangePasswordRequest request);

    /** 后台用户列表 */
    PageResult<UserAdminVO> pageUsers(String keyword, int page, int size);

    /** 启用或禁用账号 */
    void updateStatus(Long id, Integer status);

    /** 重置为初始密码 */
    void resetPassword(Long id);

    /** 管理员新建账号 */
    UserAdminVO createUser(UserCreateRequest request);

    /** 删除账号（逻辑删除） */
    void deleteUser(Long id);
}
