
export default {
    state: {
        status: "matching",
        socket: null,
        opponent_photo: "",
        opponent_username: "",
        a_id: 0,
        a_sx: 0,
        a_sy: 0,
        b_id: 0,
        b_sx: 0,
        b_sy: 0,
        gamemap: null,
        gameObject: null,
        loser: "none", // none, all, A, B
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
        updateGame(state, game) {
            state.gamemap = game.gamemap;
            state.a_id = game.a_id;
            state.a_sx = game.a_sx;
            state.a_sy = game.a_sy;
            state.b_id = game.b_id;
            state.b_sx = game.b_sx;
            state.b_sy = game.b_sy;
        },
        updateGameObject(state, gameObject) {
            state.gameObject = gameObject;
        },
        updateLoser(state, loser) {
            state.loser = loser;
        }
    },
    actions: {
    },
    modules: {
    }
}