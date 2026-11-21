# toroworld

Simulation of a toroidal world, where plants and animals compete for resources.

The world is a grid whose opposite edges are joined (a torus). Each cell holds a renewable resource. Plants feed on it, herbivores eat plants, carnivores eat herbivores. Every activity costs energy. Organisms reproduce when they have enough energy, and die when they run out of it or grow too old. Offspring inherit the parent's genome with small random changes. With the trade-off on, a species cannot improve all its traits at once.

## Build and run

Requires JDK 25 (or any JDK from 21 with `-PjavaRelease=21`).

```bash
./gradlew build                      # compile and run the tests
./gradlew run                        # open the Swing viewer (100x50 grid)
./gradlew run --args="--width 30 --height 20 --seed 7"
./gradlew run --args="--headless --ticks 500"   # print one census line per tick, then the mean traits
./gradlew run --args="--mutation off"           # offspring are exact copies
./gradlew run --args="--trade-off off"          # mutation without the trait budget
```

In the viewer: **Start/Pause**, **Step** (one tick), **Reset**, and a speed slider (ticks per second). The cell background shows the resource (darker means more depleted). Plants are green squares, herbivores are blue circles, carnivores are red circles.

## Structure

| Package | Content |
|---|---|
| `world` | Torus geometry, cell positions, the resource field |
| `life` | Organisms (plants, animals), genome and traits, activities and their costs, mutation and trade-off budget |
| `sim` | World state, behaviour (deciding), simulation loop (executing), configuration, census |
| `ui` | Swing viewer |

## Documentation

- [docs/decisions.md](docs/decisions.md): design decisions, assumptions to review, open questions
- [docs/how-to-run.md](docs/how-to-run.md): how to build, run and configure the simulation
- [docs/prompts.md](docs/prompts.md): the prompts that guided the development
