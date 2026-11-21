package io.github.brunasti.toroworld.sim;

import io.github.brunasti.toroworld.life.Activity;
import io.github.brunasti.toroworld.world.Position;

/**
 * What an organism decided to do this tick.
 *
 * @param activity the chosen activity (never {@link Activity#LIVE}, which is always paid)
 * @param target   the cell the activity applies to: destination of a move, cell of the
 *                 food, cell of the offspring; {@code null} for {@link Activity#REST}
 */
public record Intent(Activity activity, Position target) {

    public static final Intent REST = new Intent(Activity.REST, null);

    public Intent {
        if (activity == Activity.LIVE) {
            throw new IllegalArgumentException("LIVE is not a choice; it is paid every tick");
        }
        if (activity != Activity.REST && target == null) {
            throw new IllegalArgumentException(activity + " needs a target cell");
        }
    }
}
