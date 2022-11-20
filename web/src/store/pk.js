
export default {
    state: {
        status: "matching",
        socket: null,
        opponent_photo: "",
        opponent_username: "",
        gamemap: null,
    },
    getters: {
    },
    mutations: {
        updateStatus(state, status) {
            state.status = status;
        },
        updateSocket(state, socket) {
            state.socket = socket;
        },
        updateOpponent(state, opponent) {
            state.opponent_photo = opponent.opponent_photo;
            state.opponent_username = opponent.opponent_username;
        },
        updateGamemap(state, gamemap) {
            state.gamemap = gamemap;
        }
    },
    actions: {
    },
    modules: {
    }
}