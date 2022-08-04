package ver3;

import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.SubmissionPublisher;

public class JDK9FlowAsyncWithBackpressureDropExample {

    public static void main(String[] args) throws InterruptedException {
        // Create publisher with defined buffer size
        Publisher<Integer> publisherBkpE = new SubmissionPublisher<>();

        // Create Subscriber
        Subscriber<Integer> subscriberBkpE = new Subscriber<Integer>() {
            // Store subscription to request next value.
            private Subscription subscription;

            @Override
            public void onSubscribe(Subscription subscription) {
                System.out.println("Subscribed");
                // Store subscription
                this.subscription = subscription;
                subscription.request(1);
            }

            @Override
            public void onNext(Integer item) {
                // Process received value.
                System.out.println(Thread.currentThread().getName() + " | Received = " + item);
                // 100 mills delay to simulate slow subscriber
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // Processing of item is done so request next value.
                subscription.request(1);
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println(Thread.currentThread().getName() + " | ERROR = "
                        + throwable.getClass().getSimpleName() + " | " + throwable.getMessage());
            }

            @Override
            public void onComplete() {
                System.out.println("Completed");
            }
        };

        // Subscriber subscribing to publisher
        publisherBkpE.subscribe(subscriberBkpE);

        // Publish 500 numbers
        for (int i = 0; i < 500; i++) {
            System.out.println(Thread.currentThread().getName() + " | Publishing = " + i);
            ((SubmissionPublisher<Integer>) publisherBkpE).offer(i, (s, a) -> {
                s.onError(new Exception("Can't handle backpressure any more. Dropping value " + a));
                return true;
            });
        }

        // Close publisher once publishing done
        ((SubmissionPublisher<Integer>) publisherBkpE).close();

        // Since subscriber run on different thread than main thread, keep
        // main thread active for 600 seconds.
        Thread.sleep(600000);
    }
}
// In this example, we will see another strategy to handle backpressure i.e.
// dropping values in subscriber is not able to consume values at the speed of
// publisher. We will also have a handler for dropped values so that appropriate
// handling can be done. In our example, we just call error handler so that
// subscriber is aware of dropped values with proper exception & message.

// As you can see in output, since SubmissionPublisher has default buffer of
// 256, values were published till 256 values. After that values started getting
// dropped. Towards the end of the output, you can see that subscriber only
// received 256 values & rest of the values were not received because they got
// dropped.