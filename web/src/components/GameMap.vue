<template>
    <div ref="parent" class="gamemap">
        <canvas ref="canvas" tabindex="0"></canvas>
    </div>
</template>

<script>
import { GameMap } from '@/assets/scripts/GameMap';
import { ref, onMounted } from 'vue';
import { useStore } from 'vuex';

export default {
    setup() {
        let parent = ref(null);
        let canvas = ref(null);
        const store = useStore();

        onMounted(() => { // 整个页面加载完成之后需要执行的操作
            const gameObject = new GameMap(canvas.value.getContext('2d'), parent.value, store);
            store.commit("updateGameObject", gameObject);
        })

        return {
            parent,
            canvas
        }
    }
}

</script>

<style scoped>
div.gamemap {
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
}
</style>
