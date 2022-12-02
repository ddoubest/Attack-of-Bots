<template>
    <PlayGround v-if="$store.state.pk.status === 'playing'" />
    <MatchGround v-if="$store.state.pk.status === 'matching'" />
    <ResultBoard v-if="$store.state.pk.loser !== 'none'" />
</template>

<script>
import PlayGround from '@/components/PlayGround.vue'
import { onMounted, onUnmounted } from 'vue';
import { useStore } from 'vuex';
import MatchGround from '@/components/MatchGround.vue'
import ResultBoard from '@/components/ResultBoard.vue'

export default {
    components: {
        PlayGround,
        MatchGround,
        ResultBoard,
    },
    setup() {
        const store = useStore();
        const socketUrl = `ws://127.0.0.1:3000/websocket/${store.state.user.token}`;

        let socket = null;

        store.commit("updateLoser", "none");

        onMounted(() => {
            store.commit("updateOpponent", {
                opponent_photo: "https://cdn.acwing.com/media/article/image/2022/08/09/1_1db2488f17-anonymous.png",
                opponent_username: "我的对手"
            });
            socket = new WebSocket(socketUrl);
            socket.onopen = () => {
                console.log("connected!")
                store.commit("updateSocket", socket);
            };

            socket.onmessage = (msg) => {
                const data = JSON.parse(msg.data);
                if (data.event === 'match-success') {
                    store.commit("updateOpponent", {
                        opponent_photo: data.opponent_photo,
                        opponent_username: data.opponent_username
                    });
                    setTimeout(() => {
                        store.commit("updateStatus", "playing");
                    }, 500);
                    store.commit("updateGame", data.game);
                } else if (data.event === 'move') {
                    console.log(data);
                    const game = store.state.pk.gameObject;
                    const [snakeA, snakeB] = game.snakes;
                    snakeA.set_direction(data.a_direction);
                    snakeB.set_direction(data.b_direction);
                } else if (data.event === 'result') {
                    console.log(data);
                    const game = store.state.pk.gameObject;
                    const [snakeA, snakeB] = game.snakes;
                    if (data.loser === 'all' || data.loser === 'A') {
                        snakeA.status = 'die';
                    }
                    if (data.loser === 'all' || data.loser === 'B') {
                        snakeB.status = 'die';
                    }
                    store.commit("updateLoser", data.loser);
                }

            }
            socket.onclose = () => {
                console.log("disconnected!");
            };
        });

        onUnmounted(() => {
            socket.close();
            store.commit("updateStatus", "matching");
        });
    }
}

</script>

<style scoped>

</style>