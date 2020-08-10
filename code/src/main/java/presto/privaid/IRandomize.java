package presto.privaid;

import java.util.HashSet;
import java.util.Set;

public interface IRandomize {

  Set<EventInfo> randomize(EventInfo event);

  IRandomize NO_RANDOMIZATION =
      new IRandomize() {
        @Override
        public Set<EventInfo> randomize(EventInfo event) {
          Set<EventInfo> set = new HashSet<>();
          set.add(event);
          return set;
        }

        @Override
        public double getPSend() {
          return 1.0;
        }
      };

    double getPSend();
}
