package ver3;

import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.SubmissionPublisher;

public class JDK9FlowAsynchronousExample {

    public static void main(String[] args) throws InterruptedException {

        /*
         * Create a asynchronous publisher using
         * java.util.concurrent.SubmissionPublisher.SubmissionPublisher. This uses
         * ForkJoinPool.commonPool() for async.
         */
        Publisher<Integer> publisher = new SubmissionPublisher<>();

        // Create subscriber
        Subscriber<Integer> subscriber = new Subscriber<Integer>() {
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
        publisher.subscribe(subscriber);

        // Publish 500 numbers
        for (int i = 0; i < 500; i++) {
            System.out.println(Thread.currentThread().getName() + " | Publishing = " + i);
            ((SubmissionPublisher<Integer>) publisher).submit(i);
        }
        // Close publisher once publishing done
        ((SubmissionPublisher<Integer>) publisher).close();

        // Since subscriber run on different thread than main thread, keep
        // main thread active for 60 seconds.
        Thread.sleep(60000);
    }
}

// You can see in output that thread names for publisher & subscriber are
// different. Also publisher is not waiting for subscriber to finish processing.
// SubmissionPublisher has default buffer of 256, so you can see that till 256
// values publisher went without getting blocked i.e. non-blocking. But after
// 256, since buffer is full, publisher is blocked until buffer is freed up
// one-by-one.
