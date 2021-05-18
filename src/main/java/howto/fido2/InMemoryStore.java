package howto.fido2;

import io.vertx.core.Future;
import io.vertx.ext.auth.webauthn.Authenticator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InMemoryStore {

  /**
   * This is a dummy database, just for demo purposes.
   * In a real world scenario you should be using something like:
   *
   * <ul>
   *   <li>Postgres</li>
   *   <li>MySQL</li>
   *   <li>Mongo</li>
   *   <li>Redis</li>
   *   <li>...</li>
   * </ul>
   */
  private final List<Authenticator> database = new ArrayList<>();

  public Future<List<Authenticator>> fetcher(Authenticator query) {
    return Future.succeededFuture(
      database.stream()
        .filter(entry -> {
          if (query.getUserName() != null) {
            return query.getUserName().equals(entry.getUserName());
          }
          if (query.getCredID() != null) {
            return query.getCredID().equals(entry.getCredID());
          }
          // This is a bad query! both username and credID are null
          return false;
        })
        .collect(Collectors.toList())
    );
  }

  public Future<Void> updater(Authenticator authenticator) {

    long updated = database.stream()
      .filter(entry -> authenticator.getCredID().equals(entry.getCredID()))
      .peek(entry -> {
        // update existing counter
        entry.setCounter(authenticator.getCounter());
      }).count();

    if (updated > 0) {
      return Future.succeededFuture();
    } else {
      database.add(authenticator);
      return Future.succeededFuture();
    }
  }
}
