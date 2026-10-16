/**
 * 判题语言与默认代码模板。
 * 语言标识需要与后端 application.properties 中的 judge.languages.* 保持一致。
 */

export const languageOptions = [
  {
    value: 'cpp',
    label: 'C++',
    template: '#include <bits/stdc++.h>\nusing namespace std;\n\nint main() {\n    \n    return 0;\n}\n',
  },
  {
    value: 'c',
    label: 'C',
    template: '#include <stdio.h>\n\nint main() {\n    \n    return 0;\n}\n',
  },
  {
    value: 'java',
    label: 'Java',
    template: 'import java.util.Scanner;\n\npublic class Main {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        \n    }\n}\n',
  },
]

export function languageLabel(value) {
  return languageOptions.find((item) => item.value === value)?.label || value
}

export function templateOf(value) {
  return languageOptions.find((item) => item.value === value)?.template || ''
}
