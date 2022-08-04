package ver3;

import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.SubmissionPublisher;

public class JDK9FlowAsyncWithBackpressureBufferExample {

    public static void main(String[] args) throws InterruptedException {

        // Choose value in power of 2. Else SubmissionPublisher will round up to nearest
        // value of power of 2.
        final int BUFFER = 16;

        // Create publisher with defined buffer size
        Publisher<Integer> publisherBkp = new SubmissionPublisher<>(ForkJoinPool.commonPool(), BUFFER);

        // Create Subscriber
        Subscriber<Integer> subscriberBkp = new Subscriber<Integer>() {
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
        publisherBkp.subscribe(subscriberBkp);

        // Publish 100 numbers
        for (int i = 0; i < 100; i++) {
            System.out.println(Thread.currentThread().getName() + " | Publishing = " + i);
            ((SubmissionPublisher<Integer>) publisherBkp).submit(i);
        }

        // Close publisher once publishing done
        ((SubmissionPublisher<Integer>) publisherBkp).close();

        // Since subscriber run on different thread than main thread, keep
        // main thread active for 100 seconds.
        Thread.sleep(100000);
    }
}
// During asynchronous processing, if subscriber is consuming data very slow
// than publisher, this situation is called as backpressure. There are different
// ways to handle such situation gracefully. Here is an example in which
// overflow or backpressure can be handled by buffering values.



// As you can see in below output, till buffer is full i.e. 16 values, publisher
// was not blocked & publishing of values continued in non-blocking mode. But
// after buffer is full, publisher started getting blocked until previous values
// are consumed by subscriber. Buffer values should be chosen wisely so that
// publisher is not blocked during expected backpressure.