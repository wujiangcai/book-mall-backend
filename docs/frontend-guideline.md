# 前端开发规范（前台商城 + 后台管理）

> 单项目形态（前后台共存），统一规范与接口契约。基于《毕设需求.md》与现有项目文档整理。

## 1) 前后端开发需求清单（来自《毕设需求.md》）

### 前台（用户侧）
- 用户注册/登录与安全认证
- 图书浏览、分类筛选、搜索检索
- 购物车管理（增删改查）
- 订单创建、支付（模拟）与订单跟踪
- 收货地址管理

### 后台（管理端）
- 图书管理（上架/下架/编辑）、分类管理
- 订单管理（发货处理）
- 用户管理（查看、禁用/启用）
- 轮播图配置与运营内容管理

### 非功能性要求
- 输入校验、权限控制、安全认证
- 友好界面与稳定性保障
- 接口风格统一、可维护性好

---

## 2) 技术栈与工程形态

- **框架**：Vue 3
- **路由**：Vue Router
- **请求**：Axios
- **UI**：Element Plus
- **构建**：Vite

项目采用**单一前端工程**，前台/后台共存，通过目录与路由隔离。

---

## 3) 推荐项目结构（完整目录树 + 页面划分）

```text
src/
  api/
    front/                # 前台接口封装
      auth.ts
      book.ts
      cart.ts
      order.ts
      user.ts
      address.ts
      banner.ts
    admin/                # 后台接口封装
      auth.ts
      book.ts
      category.ts
      order.ts
      user.ts
      banner.ts
    request.ts            # Axios 实例与拦截器
  router/
    index.ts              # 路由入口
    front.ts              # 前台路由
    admin.ts              # 后台路由
  store/                  # 状态管理（建议 Pinia）
    auth.ts
    cart.ts
    order.ts
    admin.ts
  views/
    front/                # 前台页面
      Home.vue
      BookList.vue
      BookDetail.vue
      Cart.vue
      OrderList.vue
      OrderDetail.vue
      Login.vue
      Register.vue
      UserCenter.vue
      AddressManage.vue
    admin/                # 后台页面
      AdminLogin.vue
      Dashboard.vue
      BookManage.vue
      CategoryManage.vue
      OrderManage.vue
      UserManage.vue
      BannerManage.vue
  components/
    front/
    admin/
    common/
  utils/
    auth.ts               # token 管理
    format.ts             # 日期/金额格式化
    validate.ts           # 表单校验
  styles/
    index.css
  App.vue
  main.ts
```

> 页面名称与接口模块保持一致，前后台清晰分区。

---

## 4) 路由与鉴权规范

### 路由分区
- **前台路由**：`/`、`/books`、`/book/:id`、`/cart`、`/orders`、`/order/:id`、`/login`、`/register`、`/user`、`/address`
- **后台路由**：`/admin/login`、`/admin/dashboard`、`/admin/books`、`/admin/categories`、`/admin/orders`、`/admin/users`、`/admin/banners`

### 登录与权限
- 前台：仅公共页面免登录；购物车/订单/个人中心必须登录。
- 后台：除登录页外全部需要管理员权限。

### 401/403 处理约定
- `401`：清理 token → 跳转登录页
- `403`：提示“权限不足”，停留原页面
- `500`：提示“服务器异常”，必要时上报日志

---

## 5) API 调用统一规范

### 接口清单唯一来源
- 前台：`docs/frontend-api.md`
- 后台：`docs/admin-api.md`

### 统一响应格式（后端约定）
```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1710000000000
}
```

### Axios 基础封装示例

```ts
// src/api/request.ts
import axios from "axios";

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || "http://localhost:8003",
  timeout: 10000,
});

request.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

request.interceptors.response.use(
  (response) => {
    const { code, message, data } = response.data || {};
    if (code === 200) return data;
    return Promise.reject(new Error(message || "请求失败"));
  },
  (error) => {
    const status = error?.response?.status;
    if (status === 401) {
      localStorage.removeItem("token");
      // TODO: 跳转登录页
    }
    return Promise.reject(error);
  }
);

export default request;
```

---

## 6) 业务流程与状态管理建议

### 前台流程
1. 注册/登录 → 获取 token
2. 浏览图书 → 加入购物车
3. 地址管理 → 设置默认地址
4. 创建订单（cartIds + addressId）
5. 支付 → 进入待发货
6. 查看订单状态

### 状态管理
- `auth`：token、用户信息
- `cart`：购物车列表与数量
- `order`：订单列表与详情
- `admin`：后台筛选条件、分页状态

---

## 7) 命名与代码风格约定

- 页面/组件命名：`PascalCase`（如 `BookDetail.vue`）
- API 文件按模块命名：`book.ts` `order.ts`
- 变量/函数：`camelCase`
- 后台页面统一 `/admin/*` 路由前缀

---

## 8) 与后端的关键约束对齐

- Authorization 头传 JWT
- 统一响应格式 code=200 才视为成功
- 订单状态流转必须前端展示一致
- 软删除数据不应在前台展示

---

## 9) 参考文档

- `docs/毕设需求.md`
- `docs/design.md`
- `docs/frontend-api.md`
- `docs/admin-api.md`
- `docs/requirements.md`
