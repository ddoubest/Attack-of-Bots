import { AcGameObject } from "./AcGameObject";
import { Cell } from "./Cell";

export class Snake extends AcGameObject {
    // info：蛇的信息，gamemap：地图信息
    constructor(info, gamemap) {
        super();
        this.id = info.id;
        this.color = info.color;
        this.gamemap = gamemap;

        this.cells = [new Cell(info.r, info.c)];

        this.speed = 5; // 蛇每秒走5个格子

        this.direction = -1; // -1表示没有指令，0, 1, 2, 3表示上右下左
        this.status = "idle"; // idle表示静止，move表示正在移动，die表示已经死亡

        this.next_cell = null;

        this.dr = [-1, 0, 1, 0];
        this.dc = [0, 1, 0, -1];

        this.step = 0; // 当前回合数
        this.eps = this.speed / 75; // 不宜太小，容易导致出bug，一帧走太远，if (distance < this.eps)不执行，一直反复横跳

        this.eye_direction = 0;
        if (this.id === 1) this.eye_direction = 2;

        this.eye_dx = [
            [-1, 1],
            [1, 1],
            [1, -1],
            [-1, -1]
        ];
        this.eye_dy = [
            [-1, -1],
            [-1, 1],
            [1, 1],
            [1, -1]
        ];
    }

    start() {

    }

    set_direction(d) {
        this.direction = d;
    }

    check_snake_increasing() { // 判断当前回合，蛇是否应该增加长度
        if (this.step <= 10) return true;
        if (this.step % 3 === 1) return true;
        return false;
    }

    next_step() {
        const d = this.direction;
        this.next_cell = new Cell(this.cells[0].r + this.dr[d], this.cells[0].c + this.dc[d]);
        this.eye_direction = d;
        this.direction = -1;
        this.status = "move";
        this.step++;

        const k = this.cells.length;
        for (let i = k; i > 0; i--) {
            this.cells[i] = JSON.parse(JSON.stringify(this.cells[i - 1]));
        }
    }

    update_move() {
        const dx = this.next_cell.x - this.cells[0].x;
        const dy = this.next_cell.y - this.cells[0].y;
        const distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < this.eps) {
            this.cells[0] = this.next_cell;
            this.next_cell = null;
            this.status = 'idle';

            if (!this.check_snake_increasing()) {
                this.cells.pop();
            }
        } else {
            const move_distance = this.speed * this.timedelta / 1000;
            this.cells[0].x += move_distance * dx / distance;
            this.cells[0].y += move_distance * dy / distance;

            if (!this.check_snake_increasing()) {
                const k = this.cells.length;
                const tail = this.cells[k - 1], tail_target = this.cells[k - 2];
                const tail_dx = tail_target.x - tail.x;
                const tail_dy = tail_target.y - tail.y;
                const tail_distance = Math.sqrt(tail_dx * tail_dx + tail_dy * tail_dy);
                tail.x += move_distance * tail_dx / tail_distance;
                tail.y += move_distance * tail_dy / tail_distance;
            }
        }
    }

    update() {
        if (this.status === "move")
            this.update_move();
        this.render();
    }

    render() {
        const L = this.gamemap.L;
        const ctx = this.gamemap.ctx;

        ctx.fillStyle = this.color;

        if (this.status === 'die')
            ctx.fillStyle = 'white';

        for (const cell of this.cells) {
            ctx.beginPath();
            ctx.arc(cell.x * L, cell.y * L, L / 2 * 0.8, 0, 2 * Math.PI);
            ctx.fill();
        }

        const k = this.cells.length;
        for (let i = 1; i < k; i++) {
            const a = this.cells[i - 1], b = this.cells[i];

            if (Math.abs(a.x - b.x) < this.eps && Math.abs(a.y - b.y) < this.eps) continue;


            if (Math.abs(a.x - b.x) < this.eps) {
                const sx = a.x - 0.4, sy = Math.min(a.y, b.y);
                ctx.fillRect(sx * L, sy * L, L * 0.8, Math.abs(a.y - b.y) * L);
            } else {
                const sx = Math.min(a.x, b.x), sy = a.y - 0.4;
                ctx.fillRect(sx * L, sy * L, Math.abs(a.x - b.x) * L, L * 0.8);
            }
        }

        ctx.fillStyle = 'black';
        for (let i = 0; i < 2; i++) {
            ctx.beginPath();
            const nx = this.cells[0].x + this.eye_dx[this.eye_direction][i] * 0.15,
                ny = this.cells[0].y + this.eye_dy[this.eye_direction][i] * 0.15;
            ctx.arc(nx * L, ny * L, L * 0.05, 0, 2 * Math.PI);
            ctx.fill();
        }
    }
}