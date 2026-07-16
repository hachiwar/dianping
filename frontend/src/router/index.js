import {createRouter, createWebHistory} from 'vue-router'
import AuthPage from '@/views/AuthPage.vue'
import Home from '@/views/NearbyFood.vue'
import BusinessDetail from "@/views/BusinessDetail.vue";
import UserInfo from "@/views/UserInfo.vue";
import NearbyFood from '@/views/NearbyFood.vue'
import LifeZone from '@/views/LifeZone.vue'
import My from '@/views/My.vue'
import PackageDetail from '@/views/PackageDetail.vue'
import OrderConfirmation from '@/views/OrderConfirmation.vue'
import CouponCode from '@/views/CouponCode.vue'
import MyOrders from '@/views/MyOrders.vue'
import MyCoupons from '@/views/MyCoupons.vue'
import NewUserCoupons from '@/views/NewUserCoupons.vue'
import axios from 'axios';

const routes = [
    {
        path: '/',
        redirect: '/auth' // 默认跳转到 AuthPage
    },
    {
        path: '/auth',
        name: 'AuthPage',
        component: AuthPage
    },
    {
        path: '/my',
        name: 'My',
        component: () => import('@/views/My.vue'),
        meta: { requiresAuth: true } // 需要登录
    },
    {
        path: '/user-info',
        name: 'UserInfo',
        component: UserInfo,
    },
    {
        path: '/businessDetail/:id',
        name: 'BusinessDetail',
        component: BusinessDetail,
        meta: {requiresAuth: true}
    },
    {
        path: '/nearby-food',
        name: 'NearbyFood',
        component: NearbyFood,
        meta: {requiresAuth: true}
    },
    {
        path: '/life-zone',
        name: 'LifeZone',
        component: LifeZone,
        meta: {requiresAuth: true}
    },
    {
        path: '/package/:id',
        name: 'PackageDetail',
        component: PackageDetail,
        meta: {requiresAuth: true}
    },
    {
        path: '/order-confirmation/:packageId',
        name: 'OrderConfirmation',
        component: OrderConfirmation,
        meta: {requiresAuth: true}
    },
    {
        path: '/coupon-code/:orderId',
        name: 'CouponCode',
        component: CouponCode,
        meta: {requiresAuth: true}
    },
    {
        path: '/my-orders',
        name: 'MyOrders',
        component: MyOrders,
        meta: {requiresAuth: true}
    },
    {
        path: '/my-coupons',
        name: 'MyCoupons',
        component: MyCoupons,
        meta: {requiresAuth: true}
    },
    {
        path: '/new-user-coupons',
        name: 'NewUserCoupons',
        component: NewUserCoupons,
        meta: {requiresAuth: true}
    },
    {
        path: '/my-invitation',
        name: 'MyInvitation',
        component: () => import('@/views/MyInvitation.vue'),
        meta: { requiresAuth: true }
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 路由守卫，检查登录状态
router.beforeEach(async (to, from, next) => {
    const isAuthenticated = !!localStorage.getItem('userInfo'); // 判断用户是否已登录

    if (to.matched.some(record => record.meta.requiresAuth) && !isAuthenticated) {
        next('/auth'); // 未登录时跳转到 AuthPage
    } else if (to.name === 'NewUserCoupons') {
        try {
            const userInfo = JSON.parse(localStorage.getItem('userInfo'));
            const userId = userInfo?.id;

            if (userId) {
                const response = await axios.get('/api/orders/user', {
                    headers: { UserId: userId }
                });

                if (response.data && response.data.length > 0) {
                    alert('您已下过订单，无法访问新人优惠页面。');
                    next(false); // 阻止导航
                    return;
                }
            }
        } catch (error) {
            console.error('检查用户订单时出错:', error);
        }
    }

    next(); // 其他情况正常跳转
});

export default router