<template>
    <ContentField>
        <table class="table table-hover" style="text-align:center">
            <thead>
                <tr>
                    <th>用户</th>
                    <th>天梯分</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="user in users" :key="user.id">
                    <td>
                        <img :src="user.photo" alt="" class="user-photo">
                        &nbsp;
                        <span class="user-username">{{ user.username }}</span>
                    </td>
                    <td>
                        {{ user.rating }}
                    </td>
                </tr>
            </tbody>
        </table>

        <nav aria-label="Page navigation example" style="float: right;">
            <ul class="pagination">
                <li class="page-item">
                    <a class="page-link" href="#" @click="click_page(-2)">前一页</a>
                </li>
                <li :class="('page-item ' + page.is_active)" v-for="page of pages" :key="page.number">
                    <a class="page-link" href="#" @click="click_page(page.number)">{{ page.number }} </a>
                </li>
                <li class="page-item">
                    <a class="page-link" href="#" @click="click_page(-1)">后一页</a>
                </li>
            </ul>
        </nav>
    </ContentField>
</template>

<script>
import ContentField from '@/components/ContentField.vue';
import { useStore } from 'vuex';
import $ from 'jquery';
import { ref } from 'vue';

export default {
    components: {
        ContentField
    },
    setup() {
        const store = useStore();
        let users = ref([]);
        let current_page = 1;
        let total_users = 0;
        let pages = ref([]);

        const click_page = (page) => {
            if (page === -2) {
                page = current_page - 1;
            } else if (page === -1) {
                page = current_page + 1;
            }
            let max_pages = parseInt(Math.ceil(total_users / 10));
            if (1 <= page && page <= max_pages) {
                pull_page(page);
            }
        };

        const update_pages = () => {
            let max_pages = parseInt(Math.ceil(total_users / 10));
            let new_pages = [];

            if (current_page - 2 >= 1 && current_page + 2 <= max_pages) {
                for (let i = current_page - 2; i <= current_page + 2; i++) {
                    new_pages.push({
                        number: i,
                        is_active: i === current_page ? "active" : "",
                    });
                }
            } else if (current_page - 2 < 1) {
                for (let i = 1; i <= 5 && i <= max_pages; i++) {
                    new_pages.push({
                        number: i,
                        is_active: i === current_page ? "active" : "",
                    });
                }
            } else if (current_page + 2 > max_pages) {
                for (let i = Math.max(1, max_pages - 5 + 1); i <= max_pages; i++) {
                    new_pages.push({
                        number: i,
                        is_active: i === current_page ? "active" : "",
                    });
                }
            }

            pages.value = new_pages;
        };

        const pull_page = (page) => {
            current_page = page;
            $.ajax({
                url: "https://app4183.acapp.acwing.com.cn/api/ranklist/getlist/",
                type: "get",
                headers: {
                    Authorization: "Bearer " + store.state.user.token,
                },
                data: {
                    page: page,
                },
                success(resp) {
                    users.value = resp.users;
                    total_users = resp.users_count;
                    update_pages();
                },
                error(resp) {
                    console.log(resp);
                }
            });
        };

        pull_page(current_page);


        return {
            users,
            pages,
            click_page,
        }
    }
}

</script>

<style scoped>
img.user-photo {
    width: 4vh;
    border-radius: 50%;
}
</style>