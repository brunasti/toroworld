package it.brunasti.simulations.toroworld.sim;

import it.brunasti.simulations.toroworld.life.Activity;
import it.brunasti.simulations.toroworld.world.Position;

import java.util.List;

/**
 * What an organism decided to do this tick.
 *
 * @param activity the chosen activity (never {@link Activity#LIVE}, which is always paid)
 * @param target   the cell the activity applies to: destination of a move, cell of the
 *                 food, cell of the offspring; {@code null} for {@link Activity#REST}
 * @param route    cells to walk through, in order, before the activity: one cell per step.
 *                 For {@link Activity#MOVE} it ends at the target. For {@link Activity#EAT} a
 *                 non-empty route is a pounce: run towards the prey, then attack it.
 *                 Empty for the other activities.
 */
public record Intent(Activity activity, Position target, List<Position> route) {

    public static final Intent REST = new Intent(Activity.REST, null, List.of());

    public Intent {
        route = List.copyOf(route);
        if (activity == Activity.LIVE) {
            throw new IllegalArgumentException("LIVE is not a choice; it is paid every tick");
        }
        if (activity != Activity.REST && target == null) {
            throw new IllegalArgumentException(activity + " needs a target cell");
        }
        if (activity == Activity.MOVE && (route.isEmpty() || !route.get(route.size() - 1).equals(target))) {
            throw new IllegalArgumentException("A move must have a route ending at its target");
        }
        if ((activity == Activity.REST || activity == Activity.REPRODUCE) && !route.isEmpty()) {
            throw new IllegalArgumentException(activity + " cannot have a route");
        }
    }

    /** A single-cell move, or an activity on a target without moving. */
    public Intent(Activity activity, Position target) {
        this(activity, target, activity == Activity.MOVE ? List.of(target) : List.of());
    }

    /** A move along a route of one or more cells. */
    public static Intent move(List<Position> route) {
        return new Intent(Activity.MOVE, route.get(route.size() - 1), route);
    }

    /** Run along the route, then attack the prey at {@code prey}. */
    public static Intent pounce(List<Position> route, Position prey) {
        return new Intent(Activity.EAT, prey, route);
    }
}
