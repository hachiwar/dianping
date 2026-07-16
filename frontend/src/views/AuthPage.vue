<!-- AuthPage.vue -->
<template>
  <div class="auth-page">
    <!-- 左侧背景区域 -->
    <div class="background-section">
      <div class="background-overlay"></div>
      <img src="@/assets/background.jpg" alt="背景图" class="background-image">
      <div class="brand-info">
        <h1 class="brand-title">小众点评</h1>
        <p class="brand-slogan">发现城市中的生活美学</p>
      </div>
    </div>

    <!-- 右侧表单区域 -->
    <div class="form-section">
      <div class="auth-container">
        <!-- 登录/注册切换 -->
        <div class="auth-switch">
          <div class="switch-container">
            <h2 :class="{ active: !isLogin }" @click="switchForm('register')">注册</h2>
            <h2 :class="{ active: isLogin }" @click="switchForm('login')">登录</h2>
            <div class="slider" :class="isLogin ? 'right' : 'left'"></div>
          </div>
        </div>

        <!-- 表单容器 -->
        <div class="form-container">
          <!-- 注册表单 -->
          <transition name="slide-fade" mode="out-in">
            <form v-if="!isLogin" @submit.prevent="handleRegister" class="auth-form">
              <div class="form-item" ref="usernameInput">
                <label class="form-label">用户名</label>
                <div class="input-container">
                  <span class="icon-user"><i class="fas fa-user"></i></span>
                  <input
                      type="text"
                      v-model="registerForm.username"
                      placeholder="请输入4-20位字母、数字或下划线"
                      @input="validUsername"
                      @blur="validUsername"
                  >
                </div>
                <div class="username-wrapper">
                  <transition name="fade">
                    <span v-if="userNameMessage" class="validUsername-text" :class="{'success-message': userNameMessage.includes('可用')}">{{ userNameMessage }}</span>
                  </transition>
                </div>
              </div>
              
              <div class="form-item" ref="passwordInput">
                <label class="form-label">密码</label>
                <div class="input-container">
                  <span class="icon-lock"><i class="fas fa-lock"></i></span>
                  <input
                      :type="showPassword_register ? 'text' : 'password'"
                      v-model="registerForm.password"
                      placeholder="至少6位，必须包含字母和数字"
                      @input="checkPasswordStrength"
                      inputmode="latin"
                      @compositionstart="disableIME"
                      @compositionend="disableIME"
                  >
                  <span class="toggle-password" @click="togglePassword">
                    <i :class="showPassword_register ? 'fas fa-eye' : 'fas fa-eye-slash'"></i>
                  </span>
                </div>
                
                <!-- 密码强度显示 -->
                <div class="password-strength-wrapper">
                  <div class="password-strength">
                    <div class="strength-bar" :class="[strengthClass, { 'invalid': strengthClass === 'none' }]"
                        :style="{ width: strengthWidth }"></div>
                  </div>
                  <span class="strength-text" :class="{ 'invalid-text': strengthClass === 'none', 'success-text': strengthClass === 'strong' || strengthClass === 'very-strong' }">{{
                      passwordStrength
                    }}</span>
                </div>
              </div>
              
              <div class="form-item captcha-item" ref="captchaInput">
                <label class="form-label">验证码</label>
                <div class="captcha-container">
                  <div class="input-container captcha-input-container">
                    <input
                        v-model="registerForm.captcha"
                        placeholder="请输入验证码"
                    >
                  </div>
                  <img
                      :src="captchaUrl"
                      class="captcha-img"
                      @click="refreshCaptcha"
                      alt="验证码"
                      title="点击刷新验证码"
                  >
                </div>
                <transition name="fade">
                  <div v-if="captchaError" class="error-tip">
                    <i class="fas fa-exclamation-triangle"></i>
                    {{ captchaError }}
                  </div>
                </transition>
              </div>

              <button type="submit" class="submit-btn">
                <span>立即注册</span>
                <i class="fas fa-arrow-right"></i>
              </button>

              <!-- 用户协议 -->
              <div class="agreement">
                点击注册即表示同意
                <a href="javascript:void(0)" @click="showText('微信号: ...，电话: ...')">《用户协议》</a>和
                <a href="javascript:void(0)" @click="showText('微信号: ...，电话: ...')">《隐私政策》</a>
              </div>
            </form>

            <!-- 登录表单 -->
            <form v-else @submit.prevent="handleLogin" class="auth-form">
              <div class="form-item" ref="usernameInput">
                <label class="form-label">用户名</label>
                <div class="input-container">
                  <span class="icon-user"><i class="fas fa-user"></i></span>
                  <input
                      type="text"
                      v-model="loginForm.username"
                      @input="validLoginUsername"
                      placeholder="请输入您的用户名"
                  >
                </div>
                <div class="username-wrapper">
                  <transition name="fade">
                    <span v-if="loginUserNameError" class="validUsername-text">{{ loginUserNameError }}</span>
                  </transition>
                </div>
              </div>

              <div class="form-item" ref="passwordInput">
                <label class="form-label">密码</label>
                <div class="input-container">
                  <span class="icon-lock"><i class="fas fa-lock"></i></span>
                  <input
                      :type="showPassword_login ? 'text' : 'password'"
                      v-model="loginForm.password"
                      placeholder="请输入您的密码"
                      @input="checkPasswordStrength"
                      inputmode="latin"
                      @compositionstart="disableIME"
                      @compositionend="disableIME"
                  >
                  <span class="toggle-password" @click="togglePassword">
                    <i :class="showPassword_login ? 'fas fa-eye' : 'fas fa-eye-slash'"></i>
                  </span>
                </div>
                <div class="password-wrapper">
                  <transition name="fade">
                    <span v-if="loginPasswordError" class="validPassword-text">{{ loginPasswordError }}</span>
                  </transition>
                </div>
              </div>

              <button type="submit" class="submit-btn">
                <span>立即登录</span>
                <i class="fas fa-arrow-right"></i>
              </button>
              
              <div class="login-options">
                <div class="remember-me">
                  <input type="checkbox" id="remember" v-model="rememberMe">
                  <label for="remember">记住我</label>
                </div>
                <a href="#" class="forgot-password">忘记密码?</a>
              </div>
            </form>
          </transition>
        </div>
      </div>
      
      <!-- 底部链接 -->
      <div class="footer-links">
        <a href="https://dianping.com">关于我们</a>
        <a href="https://dianping.com">联系我们</a>
        <a href="javascript:void(0)" @click="showText('微信号: ...，电话: ...')">用户帮助</a>
        <a href="javascript:void(0)" @click="showText('微信号: ...，电话: ...')">合作伙伴</a>
        <a href="javascript:void(0)" @click="showText('微信号: ...，电话: ...')">隐私政策</a>
      </div>
      
      <transition name="fade">
        <div v-if="currentText" class="info-display">
          {{ currentText }}
        </div>
      </transition>
    </div>
  </div>
</template>
<script>
export default {
  data() {
    return {
      isLogin: true,
      captchaUrl: 'http://localhost:8080/captcha?' + Date.now(),
      passwordError: '',
      validCaptcha: false,
      captchaError: '',
      rememberMe: false,
      passwordStrengthRules: [
        {regex: /.{8,}/, score: 1},       // 长度>=8
        {regex: /.{10,}/, score: 1},      //长度>=10
        {regex: /.{15,}/, score: 1},      //长度>=15
        {regex: /[A-Z]/, score: 1},       // 包含大写字母
        {regex: /[0-9]/, score: 1},       // 包含数字
        {regex: /[^A-Za-z0-9]/, score: 3} // 包含特殊字符
      ],
      // 显示密码状态
      showPassword_login: false,
      showPassword_register: false,
      // 修改后的强度等级配置
      strengthLevels: [
        {class: 'none', text: '密码不可用，😭', width: '0'},
        {class: 'weak', text: '密码强度：弱，😥', width: '20%'},
        {class: 'medium', text: '密码强度：中，😀', width: '50%'},
        {class: 'strong', text: '密码强度：强，😄', width: '80%'},
        {class: 'very-strong', text: '密码强度：非常强，😊', width: '100%'}
      ],
      loginForm: {
        username: '',
        password: ''
      },
      registerForm: {
        username: '',
        password: '',
        captcha: ''
      },
      passwordStrength: '',
      strengthClass: 'none', 
      strengthWidth: '0',
      userNameMessage: '',
      userNameError: '',
      currentText: '',
      loginUserNameError: '',
      loginPasswordError: '',
      formAnimation: 'fade'
    }
  },
  mounted() {
    // 检查本地存储中是否有保存的用户名
    const savedUsername = localStorage.getItem('rememberedUsername');
    if (savedUsername) {
      this.loginForm.username = savedUsername;
      this.rememberMe = true;
    }
    
    // 添加页面加载动画
    setTimeout(() => {
      document.querySelector('.auth-page').classList.add('loaded');
    }, 100);
  },
  methods: {
    showText(text) {
      this.currentText = text;
      setTimeout(() => {
        this.currentText = ''
      }, 3000)
    },
    switchForm(type) {
      this.isLogin = type === 'login';
      // 重置错误信息
      this.userNameMessage = '';
      this.loginUserNameError = '';
      this.loginPasswordError = '';
      this.passwordError = '';
      this.captchaError = '';
    },
    // 注册用户名校验方法
    async validUsername() {
      // 过滤所有空格
      if (this.registerForm.username)
        this.registerForm.username = this.registerForm.username.replace(/\s/g, '')
      const username = this.registerForm.username
      if (!username) {
        this.userNameMessage = '请输入用户名'
        this.userNameError = '请输入用户名'
        return
      }
      // 优先检查空格
      if (/\s/.test(username)) {
        this.userNameMessage = '用户名不能包含空格'
        this.userNameError = '用户名不能包含空格'
        return
      }
      if (/[^\w-]/.test(username)) {
        this.userNameMessage = '用户名不能包含非法字符'
        this.userNameError = '用户名不能包含非法字符'
        return
      }
      if (username && username.length < 4) {
        this.userNameMessage = '用户名过短，应该大于4位'
        this.userNameError = '用户名过短，应该大于4位'
        return
      }
      if (username && username.length > 20) {
        this.userNameMessage = '用户名过长，应该小于20位'
        this.userNameError = '用户名过长，应该小于20位'
        return
      }
      // 格式校验
      if (!/^[a-zA-Z0-9_]{4,20}$/.test(username)) {
        this.userNameMessage = '用户名需为4-20位字母、数字或下划线'
        this.userNameError = '用户名需为4-20位字母、数字或下划线'
        return
      }
      this.userNameError = ''
      //用户名是否被注册
      try {
        const res = await this.$http.get(`/users/check-username?username=${username}`)
        if (res.data.exists) {
          this.userNameMessage = '该用户名已被注册😭'
          this.userNameError = '该用户名已被注册😭'
        } else {
          // 格式校验
          if (!/^[a-zA-Z0-9_]{4,20}$/.test(username)) {
            this.userNameMessage = '用户名需为4-20位字母、数字或下划线'
            this.userNameError = '用户名需为4-20位字母、数字或下划线'
            return
          }
          this.userNameMessage = '用户名可用😀'
          this.userNameError = ''
        }
      } catch (err) {
        this.userNameMessage = '检查失败，请重试'
        this.userNameError = '检查失败，请重试'
        console.error('用户名检查错误:', err)
      }
    }
    ,
    async validLoginUsername() {
      // 过滤所有空格
      if (this.loginForm.username)
        this.loginForm.username = this.loginForm.username.replace(/\s/g, '')
      const username = this.loginForm.username
      if (!username) {
        this.loginUserNameError = '请输入用户名'
        return
      }
      // 优先检查空格
      if (/\s/.test(username)) {
        this.loginUserNameError = '用户名不能包含空格'
        return
      }
      if (/[^\w-]/.test(username)) {
        this.loginUserNameError = '用户名不能包含非法字符'
        return
      }
      if (username && username.length < 4) {
        this.loginUserNameError = '用户名过短，应该大于4位'
        return
      }
      if (username && username.length > 20) {
        this.loginUserNameError = '用户名过长，应该小于20位'
        return
      }
      // 格式校验
      if (!/^[a-zA-Z0-9_]{4,20}$/.test(username)) {
        this.loginUserNameError = '用户名需为4-20位字母、数字或下划线'
        return
      }
      this.loginUserNameError = ''
    }
    ,
    togglePassword() {
      if (this.isLogin) this.showPassword_login = !this.showPassword_login;
      else this.showPassword_register = !this.showPassword_register;
    },
    //登录处理
    async handleLogin() {
      if (!this.loginForm.username) {
        this.loginUserNameError = '请输入用户名';
        this.triggerShake('username');
        return;
      }
      if (this.loginUserNameError) {
        this.triggerShake('username');
        return;
      }
      if (!this.loginForm.password) {
        this.loginPasswordError='密码不能为空';
        this.triggerShake('password');
        return;
      }
      try {
        // 调用登录接口
        const res = await this.$http.post('/login', {
          username: this.loginForm.username,
          password: this.loginForm.password
        });
        
        // 处理"记住我"功能
        if (this.rememberMe) {
          localStorage.setItem('rememberedUsername', this.loginForm.username);
        } else {
          localStorage.removeItem('rememberedUsername');
        }
        
        // 保存用户信息到本地存储（根据实际返回字段调整）
        localStorage.setItem('userInfo', JSON.stringify(res.data));
        
        // 添加成功动画
        this.showSuccessMessage('登录成功！正在跳转...');
        
        // 延迟跳转以显示成功消息
        setTimeout(() => {
          // 跳转到首页
          this.$router.push('/my');
        }, 1500);
      } catch (err) {
        // 处理错误信息
        const errorMsg = err.response?.data?.message || '登录失败';
        if (errorMsg.includes('用户不存在')) {
          this.loginUserNameError = '用户名不存在';
          this.triggerShake('username');
        } else if (errorMsg.includes('密码错误')) {
          this.loginPasswordError = '密码不正确😭';
          this.triggerShake('password');
        } else {
          this.$message.error(errorMsg);
        }
      }
    },

    // 密码强度计算
    checkPasswordStrength() {
      // 过滤所有空格
      if (this.registerForm.password)
        this.registerForm.password = this.registerForm.password.replace(/\s/g, '')
      if (this.loginForm.password)
        this.loginForm.password = this.loginForm.password.replace(/\s/g, '')
      const password = this.registerForm.password
      this.passwordError = ''
      // 优先检查空格
      if (/\s/.test(password)) {
        this.passwordError = '密码不能包含空格'
        this.strengthClass = 'none'
        this.strengthWidth = '0'
        this.passwordStrength = '密码不能包含空格'
        return
      }
      // 基础校验
      if (password.length < 6) {
        this.passwordStrength = '密码长度不能小于6位'
        this.strengthClass = 'none'
        this.strengthWidth = '0'
        this.passwordError = '密码不可用'
        return
      }
      if (!/\d/.test(password) || !/[a-zA-Z]/.test(password)) {
        this.passwordStrength = '密码必须同时包含字母和数字'
        this.strengthClass = 'none'
        this.strengthWidth = '0'
        this.passwordError = '密码不可用'
        return
      }
      this.passwordError = ''
      // 强度计算
      let score = 0
      this.passwordStrengthRules.forEach(rule => {
        if (rule.regex.test(password)) score += rule.score
      })
      // 根据分数确定强度等级
      let levelIndex = 0
      if (score >= 7) levelIndex = 4      // 非常强
      else if (score >= 5) levelIndex = 3  // 强
      else if (score >= 3) levelIndex = 2  // 中
      else levelIndex = 1

      this.strengthClass = this.strengthLevels[levelIndex].class
      this.passwordStrength = this.strengthLevels[levelIndex].text
      this.strengthWidth = this.strengthLevels[levelIndex].width
    }
    ,
    // 注册处理
    async handleRegister() {
      // 用户名校验
      if (!this.registerForm.username) {
        this.userNameMessage = '请输入用户名';
        this.userNameError = '请输入用户名'
        this.triggerShake('username');
        return;
      }
      if (this.userNameError) {
        this.triggerShake('username');
        return;
      }

      // 密码强度校验（确保至少达到最低要求）
      if (this.strengthClass === 'none' || this.passwordError) {
        this.passwordError = '密码不符合安全要求，请按提示修改';
        this.triggerShake('password');
        return;
      }

      // 验证码校验
      if (!this.registerForm.captcha) {
        this.captchaError = '请输入验证码';
        this.triggerShake('captcha');
        return;
      }
      try {
        // 调用注册接口
        const res = await this.$http.post('/users', {
          username: this.registerForm.username,
          password: this.registerForm.password,
          captcha: this.registerForm.captcha
        });

        // 添加成功动画
        this.showSuccessMessage('注册成功！正在跳转...');

        // 注册成功后自动登录
        localStorage.setItem('userInfo', JSON.stringify(res.data));
        
        // 延迟跳转以显示成功消息
        setTimeout(() => {
          // 跳转到首页
          this.$router.push('/my');
        }, 1500);
      } catch (err) {
        const errorMsg = err.response?.data?.message || '注册失败';

        // 特殊处理验证码错误
        if (errorMsg.includes('验证码')) {
          this.captchaError = '验证码错误';
          this.triggerShake('captcha');
          this.refreshCaptcha();
        }
        // 处理用户名已存在
        else if (errorMsg.includes('已存在')) {
          this.userNameMessage = '用户名已被注册';
          this.triggerShake('username');
        } // 其他错误处理
        else {
          this.$message.error(errorMsg);
        }
      }
    }
    ,
    // 显示成功消息
    showSuccessMessage(message) {
      // 可以根据UI组件库选择不同的实现方式
      this.$message = this.$message || {};
      if (this.$message.success) {
        this.$message.success(message);
      } else {
        alert(message);
      }
    },
    // 触发震动效果的通用方法
    triggerShake(type) {
      const refMap = {
        username: 'usernameInput',
        password: 'passwordInput',
        captcha: 'captchaInput'
      };
      const element = this.$refs[refMap[type]];
      if (!element) return;
      // 强制清除旧动画
      element.style.animation = 'none';
      // 触发浏览器重绘
      void element.offsetWidth;
      // 应用新动画
      element.style.animation = 'shake 0.5s cubic-bezier(0.36, 0.07, 0.19, 0.97) both';
      // 自动清理动画
      setTimeout(() => {
        element.style.animation = '';
      }, 500);
    }
    ,
    disableIME(event) {
      event.target.blur(); // 失去焦点，强制用户重新点击输入框
      setTimeout(() => event.target.focus(), 0); // 重新聚焦
    }
    ,
    refreshCaptcha() {
      this.captchaUrl = 'http://localhost:8080/captcha?' + Date.now();
    }
    ,
  }
}
</script>
<!-- 在.vue文件的style标签中引入 -->
<style scoped>
@import url('@/assets/css/form-style.css');
@import url('@/assets/css/error-style.css');

/* 组件特有样式可以追加在这里 */
</style>