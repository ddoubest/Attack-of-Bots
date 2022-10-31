import { AcGameObject } from './AcGameObject'
import { Wall } from './Wall';

export class GameMap extends AcGameObject {
    // ctx: 画布，parent：画布的父元素
    constructor(ctx, parent) {
        super();
        this.ctx = ctx;
        this.parent = parent;

        this.L = 0; // 地图的单位长度
        this.rows = 13;
        this.cols = 13;

        this.inner_walls_count = 20;

        this.walls = []
    }

    check_connectivity(g, sx, sy, tx, ty) {
        if (sx == tx && sy == ty) return true;
        const dx = [1, -1, 0, 0];
        const dy = [0, 0, 1, -1];
        g[sx][sy] = true;
        for (let i = 0; i < 4; i++) {
            let x = sx + dx[i], y = sy + dy[i];
            if (g[x][y]) continue;
            if (this.check_connectivity(g, x, y, tx, ty)) return true;
        }
        return false;
    }

    creata_walls() {
        const g = []
        for (let r = 0; r < this.rows; r++) {
            g[r] = []
            for (let c = 0; c < this.cols; c++) {
                g[r][c] = false;
            }
        }

        for (let r = 0; r < this.rows; r++) {
            g[r][this.cols - 1] = g[r][0] = true;
        }

        for (let c = 0; c < this.cols; c++) {
            g[this.rows - 1][c] = g[0][c] = true;
        }

        for (let i = 0; i < this.inner_walls_count;) {
            for (let j = 0; j < 1000; j++) {
                let r = parseInt(Math.random() * this.rows);
                let c = parseInt(Math.random() * this.cols);
                if (g[r][c] || g[c][r]) continue;
                //保留左下角和右上角
                if (r == 1 && c == this.cols - 2 || r == this.rows - 2 && c == 1) continue;

                if (r == c) {
                    g[r][c] = true;
                    i++;
                } else {
                    g[r][c] = g[c][r] = true;
                    i += 2;
                }
                break;
            }
        }

        const copy_g = JSON.parse(JSON.stringify(g));
        if (!this.check_connectivity(copy_g, this.rows - 2, 1, 1, this.cols - 2)) return false;

        for (let r = 0; r < this.rows; r++) {
            for (let c = 0; c < this.cols; c++) {
                if (g[r][c]) {
                    this.walls.push(new Wall(r, c, this));
                }
            }
        }

        return true;
    }

    start() {
        for (let i = 0; i < 1000; i++)
            if (this.creata_walls())
                break;
    }

    update_size() {
        this.L = parseInt(Math.min(this.parent.clientWidth / this.cols, this.parent.clientHeight / this.rows));
        this.ctx.canvas.width = this.L * this.cols;
        this.ctx.canvas.height = this.L * this.rows;
    }

    update() {
        this.update_size();
        this.render();
    }

    // 渲染函数，每次更新的时候执行
    render() {
        const color_even = '#AAD751', color_odd = '#A2D149';
        for (let r = 0; r < this.rows; r++) {
            for (let c = 0; c < this.cols; c++) {
                if ((r + c) % 2 == 0) {
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