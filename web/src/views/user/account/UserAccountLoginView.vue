<template>
    <ContentField v-if="!$store.state.user.pulling_info">
        <div class="row justify-content-md-center">
            <div class="col-3">
                <form @submit.prevent="login">
                    <div class="mb-3">
                        <label for="username" class="form-label">用户名</label>
                        <input type="text" v-model="username" class="form-control" id="username" placeholder="请输入用户名">
                    </div>
                    <div class="mb-3">
                        <label for="password" class="form-label">密码</label>
                        <input type="password" v-model="password" class="form-control" id="password"
                            placeholder="请输入密码">
                        <div class="form-text error-message">{{ error_message }}</div>
                    </div>
                    <button type="submit" class="btn btn-primary">提交</button>
                </form>
            </div>
        </div>
    </ContentField>
</template>

<script>
import ContentField from '@/components/ContentField.vue'
import { useStore } from 'vuex';
import { ref } from 'vue';
import router from '@/router/index';

export default {
    components: {
        ContentField
    },
    setup() {
        const store = useStore();
        let username = ref("");
        let password = ref("");
        let error_message = ref("");

        const jwt_token = localStorage.getItem('jwt_token');
        if (jwt_token) {
            store.commit("updateToken", jwt_token);
            store.dispatch("getInfo", {
                success() {
                    router.push({ name: "home" });
                    store.commit("updateTokenPullingInfo", false);
                },
                error() {
                    store.commit("updateTokenPullingInfo", false);
                }
            });
        } else {
            store.commit("updateTokenPullingInfo", false);
        }

        const login = () => {
            error_message.value = "";
            store.dispatch("login", {
                username: username.value,
                password: password.value,
                success() {
                    store.dispatch("getInfo", {
                        success() {
                            router.push({ name: "home" });
                        }
                    });

                },
                error() {
                    error_message.value = "用户名或密码错误";
                }
            });
        }

        return {
            username,
            password,
            error_message,
            login,
        }
    }
}

</script>

<style scoped>
button {
    width: 100%;
}

div.error-message {
    color: red;
}
</style>