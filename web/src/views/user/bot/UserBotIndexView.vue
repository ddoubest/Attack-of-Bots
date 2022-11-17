<template>
    <div class="container">
        <div class="row">
            <div class="col-3">
                <div class="card" style="margin-top:20px;">
                    <div class="card-body">
                        <img :src="$store.state.user.photo" alt="" style="width: 100%;">
                    </div>
                </div>
            </div>
            <div class="col-9">
                <div class="card" style="margin-top:20px;">
                    <div class="card-header">
                        <span style="font-size: 130%;">我的Bot</span>
                        <button type="button" class="btn btn-success float-end" data-bs-toggle="modal"
                            data-bs-target="#add-bot-btn">创建Bot</button>

                        <!-- Modal -->
                        <div class="modal fade" id="add-bot-btn" tabindex="-1">
                            <div class="modal-dialog modal-xl">
                                <div class="modal-content">
                                    <div class="modal-header">
                                        <h5 class="modal-title">创建Bot</h5>
                                        <button type="button" class="btn-close" data-bs-dismiss="modal"
                                            aria-label="Close"></button>
                                    </div>
                                    <div class="modal-body">
                                        <div class="mb-3">
                                            <label for="add-bot-title" class="form-label">名称</label>
                                            <input v-model="added_bot.title" type="text" class="form-control"
                                                id="add-bot-title" placeholder="请输入Bot名称">
                                        </div>
                                        <div class="mb-3">
                                            <label for="add-bot-description" class="form-label">简介</label>
                                            <textarea v-model="added_bot.description" class="form-control"
                                                id="add-bot-description" rows="3" placeholder="请输入Bot简介"></textarea>
                                        </div>
                                        <div class="mb-3">
                                            <label for="add-bot-content" class="form-label">代码</label>
                                            <VAceEditor v-model:value="added_bot.content" @init="editorInit"
                                                lang="c_cpp" theme="textmate" style="height: 300px"
                                                :options="{ fontSize: 18 }" />
                                        </div>
                                    </div>
                                    <div class="modal-footer">
                                        <div class="error-message">{{ added_bot.error_message }}</div>
                                        <button type="button" class="btn btn-primary" @click="add_bot">创建</button>
                                        <button type="button" class="btn btn-secondary"
                                            data-bs-dismiss="modal">取消</button>
                                    </div>
                                </div>
                            </div>
                        </div>

                    </div>
                    <div class="card-body">
                        <table class="table table-hover">
                            <thead>
                                <tr>
                                    <th>名称</th>
                                    <th>创建时间</th>
                                    <th>操作</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr v-for="bot in bots" :key="bot.id">
                                    <th>{{ bot.title }}</th>
                                    <th>{{ bot.createtime }}</th>
                                    <th>
                                        <button type="button" class="btn btn-primary" style="margin-right:10px;"
                                            data-bs-toggle="modal" :data-bs-target="'#update-bot-btn-' + bot.id"
                                            @click="update_error_message">修改</button>
                                        <button type="button" class="btn btn-danger"
                                            @click="remove_bot(bot)">删除</button>

                                        <!-- Modal -->
                                        <div class="modal fade" :id="'update-bot-btn-' + bot.id" tabindex="-1">
                                            <div class="modal-dialog modal-xl">
                                                <div class="modal-content">
                                                    <div class="modal-header">
                                                        <h5 class="modal-title">修改Bot</h5>
                                                        <button type="button" class="btn-close" data-bs-dismiss="modal"
                                                            aria-label="Close"></button>
                                                    </div>
                                                    <div class="modal-body">
                                                        <div class="mb-3">
                                                            <label for="update-bot-title" class="form-label">名称</label>
                                                            <input v-model="bot.title" type="text" class="form-control"
                                                                id="update-bot-title" placeholder="请输入Bot名称">
                                                        </div>
                                                        <div class="mb-3">
                                                            <label for="update-bot-description"
                                                                class="form-label">简介</label>
                                                            <textarea v-model="bot.description" class="form-control"
                                                                id="update-bot-description" rows="3"
                                                                placeholder="请输入Bot简介"></textarea>
                                                        </div>
                                                        <div class="mb-3">
                                                            <label for="update-bot-content"
                                                                class="form-label">代码</label>
                                                            <VAceEditor v-model:value="bot.content" @init="editorInit"
                                                                lang="c_cpp" theme="textmate" style="height: 300px"
                                                                :options="{ fontSize: 18 }" />
                                                        </div>
                                                    </div>
                                                    <div class="modal-footer">
                                                        <div class="error-message">{{ error_message_for_update }}</div>
                                                        <button type="button" class="btn btn-primary"
                                                            @click="update_bot(bot)">保存修改</button>
                                                        <button type="button" class="btn btn-secondary"
                                                            data-bs-dismiss="modal">取消修改</button>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </th>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import { useStore } from 'vuex';
import $ from "jquery"
import { ref, reactive } from 'vue';
import { Modal } from 'bootstrap/dist/js/bootstrap';

import { VAceEditor } from 'vue3-ace-editor';
import ace from 'ace-builds';

export default {
    components: {
        VAceEditor
    },
    setup() {
        ace.config.set(
            "basePath",
            "https://cdn.jsdelivr.net/npm/ace-builds@" + require('ace-builds').version + "/src-noconflict/")

        const store = useStore();

        let bots = ref([]);

        let error_message_for_update = ref("");

        const update_error_message = () => {
            error_message_for_update.value = "";
        };

        let added_bot = reactive({
            title: "",
            description: "",
            content: "",
            error_message: "",
        });

        const refresh_bots = () => {
            $.ajax({
                url: "http://127.0.0.1:3000/user/bot/getlist/",
                tyep: "get",
                headers: {
                    Authorization: "Bearer " + store.state.user.token,
                },
                success(resp) {
                    bots.value = resp;
                }
            });
        };

        refresh_bots();

        const add_bot = () => {
            added_bot.error_message = "";
            $.ajax({
                url: "http://127.0.0.1:3000/user/bot/add/",
                type: "post",
                data: {
                    title: added_bot.title,
                    description: added_bot.description,
                    content: added_bot.content
                },
                headers: {
                    Authorization: "Bearer " + store.state.user.token,
                },
                success(resp) {
                    if (resp.error_message === 'success') {
                        added_bot.title = "";
                        added_bot.description = "";
                        added_bot.content = "";
                        Modal.getInstance("#add-bot-btn").hide();
                        refresh_bots();
                    } else {
                        added_bot.error_message = resp.error_message;
                    }
                }
            });
        }

        const remove_bot = (bot) => {
            $.ajax({
                url: "http://127.0.0.1:3000/user/bot/remove/",
                type: "post",
                data: {
                    bot_id: bot.id,
                },
                headers: {
                    Authorization: "Bearer " + store.state.user.token,
                },
                success(resp) {
                    if (resp.error_message === 'success') {
                        refresh_bots();
                    }
                }
            });
        };

        /*
            update这里还有个bug没修复，就是取消修改时，bot的内容被改了，按道理应该刷新一次，
            但是对于这种随意点击屏幕退出的，不知道该怎么绑定事件
        */
        const update_bot = (bot) => {
            error_message_for_update.value = "";
            $.ajax({
                url: "http://127.0.0.1:3000/user/bot/update/",
                type: "post",
                data: {
                    bot_id: bot.id,
                    title: bot.title,
                    description: bot.description,
                    content: bot.content
                },
                headers: {
                    Authorization: "Bearer " + store.state.user.token,
                },
                success(resp) {
                    if (resp.error_message === 'success') {
                        Modal.getInstance("#update-bot-btn-" + bot.id).hide();
                        refresh_bots();
                    } else {
                        error_message_for_update.value = resp.error_message;
                    }
                }
            });
        }

        return {
            bots,
            added_bot,
            add_bot,
            remove_bot,
            update_bot,
            update_error_message,
            error_message_for_update,
        }
    }
}

</script>

<style scoped>
div.error-message {
    color: red;
    font-weight: bold;
}
</style>