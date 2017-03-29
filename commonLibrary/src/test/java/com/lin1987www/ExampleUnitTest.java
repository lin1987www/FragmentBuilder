package com.lin1987www;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.observables.ConnectableObservable;
import rx.Tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testNoDisconnectSomeoneElse() {
        ConnectableObservable<Object> source = Observable.never().publish();

        Disposable s1 = source.connect();
        Disposable s2 = source.connect();

        s1.dispose();

        boolean d1 = s1.isDisposed();
        boolean d2 = s2.isDisposed();

        Disposable s3 = source.connect();
        boolean d3 = s3.isDisposed();
        s2.dispose();

        boolean d12 = s1.isDisposed();
        boolean d22 = s2.isDisposed();
        boolean d32 = s3.isDisposed();
        assertTrue(s1.isDisposed());
        assertTrue(s2.isDisposed());
        assertFalse(s3.isDisposed());
    }

    @Test
    public void delayedUpstreamOnSubscribe() {
        final Observer<?>[] sub = {null};

        ConnectableObservable<Integer> co = new Observable<Integer>() {
            @Override
            protected void subscribeActual(Observer<? super Integer> s) {
                sub[0] = s;
            }
        }.publish();

        Disposable cod0 = co.connect();
        cod0.dispose();

        Disposable bs = new CompositeDisposable();
        sub[0].onSubscribe(bs);

        assertTrue(bs.isDisposed());
    }

    @Test
    public void NoDelayedUpstreamOnSubscribe() {
        final CompositeDisposable cd = new CompositeDisposable();

        ConnectableObservable<Integer> co = new Observable<Integer>() {
            @Override
            protected void subscribeActual(Observer<? super Integer> s) {
                s.onSubscribe(cd);
            }
        }.publish();

        boolean bsIsDisposed1 = cd.isDisposed();
        Disposable cod0 = co.connect();
        boolean bsIsDisposed2 = cd.isDisposed();
        cod0.dispose();
        boolean bsIsDisposed3 = cd.isDisposed();
        Disposable d = Disposables.empty();
        cd.add(d);
        boolean dIsDisposed = d.isDisposed();
        assertTrue(d.isDisposed());

        assertTrue(cd.isDisposed());
    }

    @Test
    public void DisposeOnSubscribe() {
        CompositeDisposable connectableObservableDisposables = new CompositeDisposable();
        final CompositeDisposable observableDisposable = new CompositeDisposable();
        ConnectableObservable<Integer> connectableObservable = new Observable<Integer>() {
            @Override
            protected void subscribeActual(Observer<? super Integer> s) {
                s.onSubscribe(observableDisposable);
            }
        }.publish();
        Disposable connectableObservableDisposable = connectableObservable.connect();
        connectableObservableDisposables.add(connectableObservableDisposable);

        final Disposable[] ods = new Disposable[1];
        Observer<Object> observer = new Observer<Object>() {
            @Override
            public void onSubscribe(Disposable d) {
                ods[0] = d;
            }

            @Override
            public void onNext(Object t) {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        };
        connectableObservable.subscribe(observer);
        Disposable observerDisposable = ods[0];
        boolean codIsDisposed1 = connectableObservableDisposable.isDisposed();
        boolean cdIsDisposed1 = observableDisposable.isDisposed();
        boolean odIsDisposed1 = observerDisposable.isDisposed();
        connectableObservableDisposables.dispose();
        boolean codIsDisposed2 = connectableObservableDisposable.isDisposed();
        boolean cdIsDisposed2 = observableDisposable.isDisposed();
        boolean odIsDisposed2 = observerDisposable.isDisposed();
    }

    @Test
    public void disposeTasks() {
        final AtomicInteger atomicInteger = new AtomicInteger(0);
        final Disposable[] ds = new Disposable[1];
        CountDownLatch latch = new CountDownLatch(1);
        class TestTasks extends Tasks<TestTasks> {
            @Override
            protected void subscribeActual() {
                subTask(
                        Observable.timer(500, TimeUnit.MILLISECONDS),
                        this::task1
                );
            }

            protected void task1(long time) {
                atomicInteger.incrementAndGet();
                subTask(
                        Observable.timer(100, TimeUnit.MILLISECONDS),
                        this::task2
                );
            }

            protected void task2(long time) {
                ds[0].dispose();
                atomicInteger.incrementAndGet();
                subTask(
                        Observable.timer(100, TimeUnit.MILLISECONDS),
                        this::task3
                );
            }

            protected void task3(long time) {
                atomicInteger.incrementAndGet();
                done();
            }
        }
        TestTasks testTasks = new TestTasks();
        Disposable disposable = testTasks.subscribe(
                (tasks) -> {
                    assertTrue(atomicInteger.get() == 3);
                },
                (ex) -> {
                },
                () -> {
                    atomicInteger.incrementAndGet();
                    assertTrue(atomicInteger.get() == 4);

                }
        );
        ds[0] = disposable;
        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (Throwable e) {
        }
    }
}