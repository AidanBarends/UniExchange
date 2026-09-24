/*
  notificationEvents - a tiny pub/sub so the TopBar bell's unread count and
  the /notifications page stay in sync without wiring up a context provider
  for one small piece of shared state.

  Usage:
    - NotificationsPage calls emitNotificationsChanged() after any
      markRead / mark-all-read call succeeds.
    - TopBar calls onNotificationsChanged(refetchCount) once on mount and
      refetches immediately whenever the event fires, instead of waiting for
      its own poll interval to come around.

  Deliberately not a full event system - one event, no payload. If this
  grows more event types, promote it to a proper context.

  OWNER: Joshua Reid Adams (230317693)
*/

type Listener = () => void;

const listeners = new Set<Listener>();

export function emitNotificationsChanged(): void {
  for (const listener of listeners) listener();
}

/** Returns an unsubscribe function - call it from a useEffect cleanup. */
export function onNotificationsChanged(listener: Listener): () => void {
  listeners.add(listener);
  return () => {
    listeners.delete(listener);
  };
}
