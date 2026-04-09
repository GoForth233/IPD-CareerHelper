# Style Library (Vue / uni-app)

该目录是 C 端项目的组件库入口，用于统一声明“前端页面由组件库组件搭建”。

## 目录

- `components/`
  - `SlPage.vue` 页面容器
  - `SlSection.vue` 区块容器
  - `SlCard.vue` 卡片容器
  - `SlSectionTitle.vue` 区块标题
  - `SlIconButton.vue` 图标按钮
  - `SlButton.vue` 通用按钮
  - `SlListItem.vue` 列表项
  - `SlInput.vue` 输入框
  - `index.ts` 统一导出

## 使用方式

```ts
import { SlCard, SlButton, SlInput } from '@/style-library/components';
```

说明：
- 当前前端业务页面已完成，本组件库用于组件化归档与复用封装。
- 后续新页面/重构页面可优先从该目录取组件，维持统一口径。
