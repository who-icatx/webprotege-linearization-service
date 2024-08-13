package edu.stanford.protege.webprotege.liniarizationservice.services;

import edu.stanford.protege.webprotege.liniarizationservice.config.ReadWriteLockConfig;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.*;
import org.mockito.quality.Strictness;

import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReadWriteLockServiceTest {

    private ReadWriteLockService readWriteLockService;
    private ReadWriteLockConfig config;
    private ReentrantReadWriteLock.ReadLock readLock;
    private ReentrantReadWriteLock.WriteLock writeLock;

    @BeforeEach
    void setUp() {
        config = mock(ReadWriteLockConfig.class);
        ReentrantReadWriteLock readWriteLock = mock(ReentrantReadWriteLock.class);
        readLock = mock(ReentrantReadWriteLock.ReadLock.class);
        writeLock = mock(ReentrantReadWriteLock.WriteLock.class);

        when(config.getTimeout()).thenReturn(1);
        when(config.getTimeUnit()).thenReturn(TimeUnit.SECONDS);
        when(config.getMaxRetries()).thenReturn(3);

        when(readWriteLock.readLock()).thenReturn(readLock);
        when(readWriteLock.writeLock()).thenReturn(writeLock);

        readWriteLockService = new ReadWriteLockServiceImpl(config, readWriteLock);
    }

    @Test
    void GIVEN_timeoutExceptionInReadOperation_WHEN_executeReadLock_THEN_throwRuntimeException() throws Exception {
        Callable<String> readOperation = () -> "readSuccess";
        when(readLock.tryLock(1, TimeUnit.SECONDS)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> readWriteLockService.executeReadLock(readOperation));

        verify(readLock, times(3)).tryLock(1, TimeUnit.SECONDS);
    }

    @Test
    void GIVEN_timeoutExceptionInWriteOperation_WHEN_executeWriteLock_THEN_throwRuntimeException() throws Exception {
        Callable<String> writeOperation = () -> "writeOperation";
        when(writeLock.tryLock(1, TimeUnit.SECONDS)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> readWriteLockService.executeWriteLock(writeOperation));

        verify(writeLock, times(3)).tryLock(1, TimeUnit.SECONDS);
    }

    @Test
    void GIVEN_readOperation_WHEN_executeReadLock_THEN_returnResult() throws Exception {
        Callable<String> readOperation = () -> "readSuccess";
        when(readLock.tryLock(1, TimeUnit.SECONDS)).thenReturn(true);

        String result = readWriteLockService.executeReadLock(readOperation);
        assertEquals("readSuccess", result);

        verify(readLock).unlock();
    }

    @Test
    void GIVEN_writeOperation_WHEN_executeWriteLock_THEN_returnResult() throws Exception {
        Callable<String> writeOperation = () -> "writeSuccess";
        when(writeLock.tryLock(1, TimeUnit.SECONDS)).thenReturn(true);

        String result = readWriteLockService.executeWriteLock(writeOperation);
        assertEquals("writeSuccess", result);

        verify(writeLock).unlock();
    }

    @Test
    void GIVEN_writeOperationRunnable_WHEN_executeWriteLock_THEN_executeRunnable() throws InterruptedException {
        Runnable writeOperation = mock(Runnable.class);
        when(writeLock.tryLock(1, TimeUnit.SECONDS)).thenReturn(true);

        readWriteLockService.executeWriteLock(writeOperation);

        verify(writeOperation, times(1)).run();
        verify(writeLock).unlock();
    }

    @Test
    void GIVEN_unexpectedExceptionInReadOperation_WHEN_executeReadLock_THEN_throwRuntimeException() throws Exception {
        Callable<String> readOperation = () -> {
            throw new Exception("Unexpected");
        };
        when(readLock.tryLock(1, TimeUnit.SECONDS)).thenReturn(true);

        assertThrows(RuntimeException.class, () -> readWriteLockService.executeReadLock(readOperation));

        verify(readLock).unlock();
    }

    @Test
    void GIVEN_unexpectedExceptionInWriteOperation_WHEN_executeWriteLock_THEN_throwRuntimeException() throws Exception {
        Callable<String> writeOperation = () -> {
            throw new Exception("Unexpected");
        };
        when(writeLock.tryLock(1, TimeUnit.SECONDS)).thenReturn(true);

        assertThrows(RuntimeException.class, () -> readWriteLockService.executeWriteLock(writeOperation));

        verify(writeLock).unlock();
    }

    @Test
    void GIVEN_writeOperationWithRetries_WHEN_executeWriteLock_THEN_exceedsMaxRetries() throws Exception {
        Callable<String> writeOperation = mock(Callable.class);
        when(writeLock.tryLock(1, TimeUnit.SECONDS)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> readWriteLockService.executeWriteLock(writeOperation));

        verify(writeLock, times(3)).tryLock(1, TimeUnit.SECONDS);
        verify(writeOperation, never()).call();
    }

    @Test
    void GIVEN_writeOperationRunnableWithRetries_WHEN_executeWriteLock_THEN_exceedsMaxRetries() throws InterruptedException {
        Runnable writeOperation = mock(Runnable.class);
        when(writeLock.tryLock(1, TimeUnit.SECONDS)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> readWriteLockService.executeWriteLock(writeOperation));

        verify(writeLock, times(3)).tryLock(1, TimeUnit.SECONDS);
        verify(writeOperation, never()).run();
    }

    @Test
    void GIVEN_multipleThreads_WHEN_executeWriteLock_THEN_onlyOneThreadExecutesAtATime() throws InterruptedException, ExecutionException {
        readWriteLockService = new ReadWriteLockServiceImpl(config, new ReentrantReadWriteLock(true));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch firstThreadAcquiredLock = new CountDownLatch(1);
        CountDownLatch firstThreadCompleted = new CountDownLatch(1);

        Callable<String> firstThreadTask = () ->
                readWriteLockService.executeWriteLock(() ->
                        {
                            firstThreadAcquiredLock.countDown();
                            try {
                                Thread.sleep(2000); // Hold the lock for 2 seconds
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            firstThreadCompleted.countDown();
                            return "firstThread";
                        }
                );

        Callable<String> secondThreadTask = () ->
                readWriteLockService.executeWriteLock(() -> {
                    //Verify the first thread completed before we started executing action from second thread
                    assertEquals(0, firstThreadCompleted.getCount());
                    return "secondThread";
                });

        Future<String> firstThreadFuture = executor.submit(firstThreadTask);
        firstThreadAcquiredLock.await(); // Wait for the first thread to acquire the lock
        Future<String> secondThreadFuture = executor.submit(secondThreadTask);
        firstThreadCompleted.await();

        // Verify the first thread completed successfully
        String firstThreadResult = firstThreadFuture.get();
        assertEquals("firstThread", firstThreadResult);

        // Verify the second thread completed successfully after the first thread released the lock
        String secondThreadResult = secondThreadFuture.get();
        assertEquals("secondThread", secondThreadResult);

        executor.shutdown();
    }

    @Test
    void GIVEN_writeOperationFailsToAcquireLock_WHEN_executeWriteLock_THEN_throwTimeoutException() throws InterruptedException {
        Callable<String> writeOperation = () -> "writeOperation";
        when(writeLock.tryLock(1, TimeUnit.SECONDS)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> readWriteLockService.executeWriteLock(writeOperation));

        verify(writeLock, times(3)).tryLock(1, TimeUnit.SECONDS);
    }

    @Test
    void GIVEN_writeOperationRunnableFailsToAcquireLock_WHEN_executeWriteLock_THEN_throwTimeoutException() throws InterruptedException {
        Runnable writeOperation = mock(Runnable.class);
        when(writeLock.tryLock(1, TimeUnit.SECONDS)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> readWriteLockService.executeWriteLock(writeOperation));

        verify(writeLock, times(3)).tryLock(1, TimeUnit.SECONDS);
        verify(writeOperation, never()).run();
    }
}
