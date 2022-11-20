import { AcGameObject } from './AcGameObject'
import { Snake } from './Snake';
import { Wall } from './Wall';

export class GameMap extends AcGameObject {
    // ctx: 画布，parent：画布的父元素
    constructor(ctx, parent, store) {
        super();
        this.ctx = ctx;
        this.parent = parent;
        this.store = store;

        this.L = 0; // 地图的单位长度
        this.rows = 13;
        this.cols = 14;

        this.inner_walls_count = 20;

        this.walls = []

        this.snakes = [
            new Snake({ id: 0, color: "#4876EC", r: this.rows - 2, c: 1 }, this),
            new Snake({ id: 1, color: "#F94848", r: 1, c: this.cols - 2 }, this),
        ]
    }

    creata_walls() {
        const g = this.store.state.pk.gamemap;

        for (let r = 0; r < this.rows; r++) {
            for (let c = 0; c < this.cols; c++) {
                if (g[r][c]) {
                    this.walls.push(new Wall(r, c, this));
                }
            }
        }
    }

    add_listening_events() {
        this.ctx.canvas.focus();
        const [snkae0, snake1] = this.snakes;
        this.ctx.canvas.addEventListener("keydown", e => {
            if (e.key === 'w') snkae0.set_direction(0);
            else if (e.key === 'd') snkae0.set_direction(1);
            else if (e.key === 's') snkae0.set_direction(2);
            else if (e.key === 'a') snkae0.set_direction(3);
            else if (e.key === 'ArrowUp') snake1.set_direction(0);
            else if (e.key === 'ArrowRight') snake1.set_direction(1);
            else if (e.key === 'ArrowDown') snake1.set_direction(2);
            else if (e.key === 'ArrowLeft') snake1.set_direction(3);
        });
    }

    start() {
        this.creata_walls();

        this.add_listening_events();
    }

    update_size() {
        this.L = parseInt(Math.min(this.parent.clientWidth / this.cols, this.parent.clientHeight / this.rows));
        this.ctx.canvas.width = this.L * this.cols;
        this.ctx.canvas.height = this.L * this.rows;
    }

    check_ready() {
        for (const snake of this.snakes) {
            if (snake.status !== "idle") return false;
            if (snake.direction === -1) return false;
        }
        return true;
    }

    next_step() {
        for (const snake of this.snakes) {
            snake.next_step();
        }
    }

    check_valid(cell) {
        for (const wall of this.walls) {
            if (cell.r === wall.r && cell.c === wall.c) return false;
        }

        for (const snake of this.snakes) {
            let k = snake.cells.length;
            if (!snake.check_snake_increasing()) k--;

            for (let i = 0; i < k; i++)
                if (cell.r === snake.cells[i].r && cell.c === snake.cells[i].c)
                    return false;
        }
        return true;
    }

    update() {
        this.update_size();
        if (this.check_ready()) {
            this.next_step();
        }
        this.render();
    }

    // 渲染函数，每次更新的时候执行
    render() {
        const color_even = '#AAD751', color_odd = '#A2D149';
        for (let r = 0; r < this.rows; r++) {
            for (let c = 0; c < this.cols; c++) {
                if ((r + c) % 2 === 0) {
                    this.ctx.fillStyle = color_even;
                } else {
                    this.ctx.fillStyle = color_odd;
                }
                // 参数意思：起始横坐标、起始纵坐标、宽度、长度
                this.ctx.fillRect(c * this.L, r * this.L, this.L, this.L);
            }
        }
    }

}