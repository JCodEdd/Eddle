package com.search.engine.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class SchedulerConfigTest {

  @Test
  public void testTaskSchedulerBean() {
    SchedulerConfig schedulerConfig = new SchedulerConfig();

    ThreadPoolTaskScheduler taskScheduler = (ThreadPoolTaskScheduler) schedulerConfig.taskScheduler();

    assertThat(taskScheduler).isNotNull();
    assertThat(taskScheduler.getPoolSize()).isEqualTo(10);
    assertThat(taskScheduler.getThreadNamePrefix()).isEqualTo("task-scheduler");
  }

}