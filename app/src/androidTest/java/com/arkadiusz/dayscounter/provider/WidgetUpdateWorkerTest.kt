package com.arkadiusz.dayscounter.provider

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestWorkerBuilder
import com.arkadiusz.dayscounter.data.repository.DatabaseRepository
import com.arkadiusz.dayscounter.data.model.Event
import com.arkadiusz.dayscounter.data.worker.WidgetUpdateWorker
import junit.framework.TestCase.fail
import org.junit.Assert.assertArrayEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@RunWith(AndroidJUnit4::class)
class WidgetUpdateWorkerTest {
    private lateinit var context: Context
    private lateinit var executor: Executor
    private lateinit var worker: WidgetUpdateWorker
    private lateinit var eventsList: List<Event>

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        executor = Executors.newSingleThreadExecutor()

        worker = TestWorkerBuilder<WidgetUpdateWorker>(
            context = context,
            executor = executor
        ).build()
    }

    @Test
    fun shouldUpdateCorrectWidgets() {
        //given list of events with widgets
        eventsList = DatabaseRepository().getEventsWithWidgets()

        //when running WorkManager
        val result = worker.doWork()

        //should return correct array of updated widgets
        if (result is ListenableWorker.Result.Success) {
            assertArrayEquals(result.outputData.getIntArray("widgetIds"), eventsList
                .map { it.widgetID }.toIntArray()
            )
        } else {
            fail()
        }
    }
}

