# How to configure and run Toroworld

## 1. Requirements

- **JDK 25.** Any JDK from 21 also works if you add `-PjavaRelease=21` to the Gradle commands.
- **No Gradle installation is needed.** The wrapper (`./gradlew`) downloads Gradle 9.7.1 the first time it runs.
- **An internet connection for the first build,** to download Gradle, JUnit and commonmark-java (the Markdown library used by the manual window).

## 2. Build and test

```bash
./gradlew build          # compile and run all unit tests
./gradlew test           # tests only
```

The test report is in `build/reports/tests/test/index.html`.

## 3. Run

There are two ways to run the simulation.

### With the viewer (Swing)

```bash
./gradlew run
```

The window has these controls:

| Control | Effect |
|---|---|
| **Start / Pause** | Runs the simulation continuously, or stops it |
| **Step** | Advances exactly one tick |
| **Reset** | Restarts from tick 0 with the same configuration and seed |

A run lasts a fixed number of ticks (`--ticks`, default 1000). When the last tick is reached, the run pauses, **Start** and **Step** are disabled, and the bar shows "(end)". **Reset** starts a new run.
| **Speed** | Ticks per second, from 1 to 50 |
| **Manual** | Opens the user manual ([manual.md](manual.md)) in a separate window. Links to the other documents open in the same window; **Manual home** goes back. |

How the grid is drawn:

| What you see | Meaning |
|---|---|
| Cell background, light brown → light grey → medium grey | Resource in the cell, full → low → exhausted. A medium grey cell is empty and has no resource left. |
| Green square | Plant. Brighter means closer to its reproduction threshold. |
| Blue circle | Herbivore |
| Red circle | Carnivore |

See [manual.md](manual.md), section 9, for a legend image.

The status bar at the bottom shows, on the first line, the current tick and the total number of ticks of the run (e.g. `Tick 15 / 1000`), the population counts and the total resource. The second line shows the grid size, the seed and the mutation settings.

### Headless (no window)

```bash
./gradlew run --args="--headless --ticks 500"
```

This prints one census line per tick. At the end it prints the mean value of each trait for every species still alive, which is how you can see evolution happening. The run stops early if all life ends.

## 4. Command-line options

You can pass these options with `--args="..."` (Gradle) or as program arguments (IntelliJ, see section 6).

| Option | Default | Meaning |
|---|---|---|
| `--width N` | 100 | Grid width |
| `--height N` | 50 | Grid height |
| `--seed N` | 42 | Random seed. The same seed and configuration always give the same run. |
| `--mutation on\|off` | on | `off`: offspring are exact copies of their parent |
| `--trade-off on\|off` | on | `off`: mutation without the trait budget |
| `--carnivore-max-speed N` | 3 | Highest speed carnivores can evolve, in cells per tick (at least 1) |
| `--headless` | (viewer) | Run without a window, printing the census |
| `--ticks N` | 1000 | Length of the run, in ticks. The viewer pauses at the last tick. |
| `--help` | | Print the usage |

Example:

```bash
./gradlew run --args="--width 30 --height 20 --seed 7 --trade-off off --headless --ticks 2000"
```

> The starting populations are **densities**, so they follow the grid size: a larger grid gets proportionally more organisms (see section 5.1).

## 5. Where the parameters are set

There is no configuration file yet (see open question O3 in [decisions.md](decisions.md)). Every parameter that is not on the command line is set **in code**, in the files below. Change the value, then rebuild and run.

All paths below are relative to `src/main/java/it/brunasti/simulations/toroworld/`.

### 5.1 World and species: `sim/SimulationConfig.java`, method `defaults()`

```java
return new SimulationConfig(
        100, 50, 42L,                // width, height, seed
        5.0, 10.0, 1.0,              // resource: initial, maximum, regeneration per tick
        new SpeciesConfig(...),      // plants
        new SpeciesConfig(...),      // herbivores
        new SpeciesConfig(...),      // carnivores
        MutationSettings.DEFAULT);   // mutation, see 5.2
```

**Resource** (the same for every cell):

| Parameter | Default | Meaning |
|---|---|---|
| initial | 5.0 | Resource in each cell at tick 0 |
| maximum | 10.0 | Regeneration never goes above this |
| regeneration | 1.0 | Added to every cell at the start of each tick |

**Each species** is a `SpeciesConfig(initialDensity, initialEnergy, genome, costs, efficiency, mobility)`. Without the last argument, the species gets `Mobility.FIXED` (speed 1, sees only its neighbours). The carnivores, for example:

```java
new SpeciesConfig(0.04, 20.0,                               // initial density, initial energy
        new Genome(1.0, 15.0, 30.0, 0.4, 120, 1.0, 1.5),    // starting genome
        new ActivityCosts(0.6, 0.0, 0.6, 0.3, 3.0),         // costs: live, rest, move, eat, reproduce
        0.6,                                                // efficiency
        new Mobility(3.0, 1.5, 3.0,                         // max speed, min vision, max vision
                Mobility.VisionBase.OWN_SPEED))             // sight = vision x own speed
```

| Parameter | Plants | Herbivores | Carnivores | Meaning |
|---|---|---|---|---|
| initialDensity | 0.50 | 0.20 | 0.04 | Fraction of the cells that get one organism at tick 0, placed at random. 0.20 means 20 per 100 cells. The count is rounded to the nearest whole organism: on 100x50 that is 2,500 plants, 1,000 herbivores and 200 carnivores. |
| initialEnergy | 5.0 | 10.0 | 20.0 | Energy of each starting organism |
| efficiency | 0.8 | 0.7 | 0.6 | Fraction of the food taken in that becomes energy |

**Genome**, `Genome(metabolism, intake, reproductionThreshold, offspringShare, maxAge, speed, vision)`. The five-argument form sets speed and vision to 1. This is the **founder** genome. With mutation on, offspring drift away from it.

| Trait | Plants | Herbivores | Carnivores | Meaning |
|---|---|---|---|---|
| metabolism | 1.0 | 1.0 | 1.0 | Multiplier on every activity cost |
| intake | 3.0 | 1.5 | 15.0 | Maximum food per feeding: resource (plants), plant energy (herbivores), prey energy (carnivores) |
| reproductionThreshold | 8.0 | 25.0 | 30.0 | Energy needed before trying to reproduce |
| offspringShare | 0.4 | 0.4 | 0.4 | Share of the parent's remaining energy given to the offspring |
| maxAge | 60 | 80 | 120 | Age in ticks after which the organism dies |
| speed | 1 | 1 | 1.0 | Cells travelled per tick. A fraction gives a matching chance of one more cell. |
| vision | 1 | 1 | 1.5 | Sight as a multiple of the base speed (see Mobility below) |

**Activity costs**, `ActivityCosts(live, rest, move, eat, reproduce)`. Each tick an organism pays the cost of the one activity it chose **plus** `live`, all multiplied by its metabolism.

| Activity | Plants | Herbivores | Carnivores | When it is paid |
|---|---|---|---|---|
| live | 0.5 | 0.4 | 0.6 | Every tick, always |
| rest | 0.0 | 0.0 | 0.0 | When doing nothing else |
| move | 0.0 | 0.3 | 0.6 | Moving, **per cell travelled** (plants never move) |
| eat | 0.2 | 0.2 | 0.3 | Feeding |
| reproduce | 1.0 | 2.0 | 3.0 | Producing one offspring |

> The defaults are a first guess, not a calibration. They were adjusted on a 10x10 grid, where plants and herbivores usually coexist and carnivores do not survive (open question O2).

**Mobility**, `Mobility(maxSpeed, minVision, maxVision, visionBase)`: how the species moves and sees, and the ranges its speed and vision can evolve in.

| Parameter | Plants and herbivores (`FIXED`) | Carnivores | Meaning |
|---|---|---|---|
| maxSpeed | 1 | 3 | Highest speed the species can evolve (the lowest is always 1). Also `--carnivore-max-speed`. |
| minVision, maxVision | 1, 1 | 1.5, 3 | Range the vision trait can evolve in |
| visionBase | `OWN_SPEED` | `OWN_SPEED` | `OWN_SPEED`: sight = floor(vision × own speed). `MAX_SPEED`: sight = floor(vision × maxSpeed), the same for every individual. |

The founder genome must lie within these ranges, otherwise the configuration is rejected at start-up.

### 5.2 Mutation and trade-off: `life/MutationSettings.java`, constant `DEFAULT`

```java
new MutationSettings(true, 0.1, 0.05, true, 0.0)
//                   enabled, probability, relativeSigma, tradeOff, allowance
```

| Parameter | Default | Meaning |
|---|---|---|
| enabled | true | Same as `--mutation` |
| probability | 0.1 | Chance that each trait changes at birth |
| relativeSigma | 0.05 | Size of a change: a normal distribution with σ = 5% of the value |
| tradeOff | true | Same as `--trade-off` |
| allowance | 0.0 | How much total advantage over the founder genome is allowed. At 0, no genome can be better than its founder overall; improving one trait must be paid for by another. |

How the budget is computed is explained in [decisions.md](decisions.md) (G4) and in the Javadoc of `life/TraitBudget.java`.

### 5.3 Valid range of each trait: `life/Trait.java`

Mutation clamps every trait to its range:

| Trait | Minimum | Maximum | In the budget? |
|---|---|---|---|
| METABOLISM | 0.05 | 20 | yes, lower is better |
| INTAKE | 0 | 1000 | yes, higher is better |
| REPRODUCTION_THRESHOLD | 0.1 | 10,000 | no (strategy) |
| OFFSPRING_SHARE | 0.05 | 0.95 | no (strategy) |
| MAX_AGE | 1 | 100,000 | yes, higher is better |
| SPEED | 1 | the species' maxSpeed | yes, higher is better |
| VISION | the species' minVision | the species' maxVision | yes, higher is better |

### 5.4 Behaviour rules: `sim/SimpleBehaviour.java`

This class holds the decision rules (reproduce → eat → hunt → move → rest), described in its Javadoc. To try different rules, write another implementation of `sim/Behaviour` and pass it to the `Simulation(config, behaviour)` constructor.

### 5.5 In your own code or tests

`SimulationConfig` has `with...` methods to change a copy of the defaults without editing `defaults()`:

```java
SimulationConfig config = SimulationConfig.defaults()
        .withSize(30, 30)
        .withSeed(7)
        .withDensities(0.25, 0.08, 0.01)     // plants, herbivores, carnivores (fractions of the cells)
        .withMutation(MutationSettings.DEFAULT.withTradeOff(false));

Simulation simulation = new Simulation(config);
for (int t = 0; t < 1000; t++) {
    System.out.println(simulation.step());   // returns the Census after the tick
}
System.out.println(simulation.meanTraits(Species.PLANT));
```

## 6. Running from IntelliJ IDEA

1. Open the project folder and, when IntelliJ offers it, **load it as a Gradle project**. If it does not offer, right-click `build.gradle.kts` and choose *Link Gradle Project*.
2. Set the project SDK to JDK 25 (*File → Project Structure → Project*).
3. Run the class `it.brunasti.simulations.toroworld.Toroworld`. Put the options from section 4 in *Run → Edit Configurations → Program arguments*.
4. Alternatively, run the Gradle task *application → run* from the Gradle tool window.

## 7. Reproducibility

A run depends only on the configuration and the seed. To repeat a run exactly, keep both the same. To see whether a result is real or just luck, run several seeds:

```bash
for s in 1 2 3 4 5; do ./gradlew -q run --args="--headless --ticks 1000 --seed $s" | tail -4; done
```
