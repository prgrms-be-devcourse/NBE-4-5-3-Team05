import { resolve } from "path";
import React, {
  createContext,
  useContext,
  useEffect,
  useRef,
  useState,
} from "react";

interface EventSourceContextValue {
  eventSource: EventSource | null;
  connect: (url: string) => Promise<EventSource>;
  disconnect: () => void;
}

const EventSourceContext = createContext<EventSourceContextValue | undefined>(
  undefined
);

export const EventSourceProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  const [eventSource, setEventSource] = useState<EventSource | null>(null);
  const eventSourceRef = useRef<EventSource | null>(null);

  const connect = (url: string) => {
    return new Promise<EventSource>((resolve, reject) => {
      if (eventSourceRef.current) {
        console.warn("EventSource is already connected.");
        resolve(eventSourceRef.current);
        return;
      }

      const es = new EventSource(url, {
        withCredentials: true,
      });

      es.onopen = () => {
        eventSourceRef.current = es;
        setEventSource(es);
        console.log("EventSource connected.");
        resolve(es);
      };
      es.onerror = (error) => {
        console.error("EventSource error:", error);
        disconnect();
        reject(error);
      };
    });
  };
  const disconnect = () => {
    if (eventSourceRef.current) {
      eventSourceRef.current.close();
      eventSourceRef.current = null;
      setEventSource(null);
      console.log("EventSource disconnected.");
    }
  };

  useEffect(() => {
    return () => {
      disconnect();
    };
  }, []);

  return (
    <EventSourceContext.Provider value={{ eventSource, connect, disconnect }}>
      {children}
    </EventSourceContext.Provider>
  );
};

export const useEventSource = (): EventSourceContextValue => {
  const context = useContext(EventSourceContext);
  if (!context) {
    throw new Error(
      "useEventSource must be used within an EventSourceProvider"
    );
  }
  return context;
};
