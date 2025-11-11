package com.sleekydz86.strategy.strategy;

import com.sleekydz86.strategy.global.strategy.CrudStrategy;
import com.sleekydz86.strategy.global.strategy.StrategyFactory;
import com.sleekydz86.strategy.global.strategy.impl.DeleteStrategy;
import com.sleekydz86.strategy.global.strategy.impl.InsertStrategy;
import com.sleekydz86.strategy.global.strategy.impl.UpdateStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StrategyFactoryTest {

    @Mock
    private InsertStrategy insertStrategy;

    @Mock
    private UpdateStrategy updateStrategy;

    @Mock
    private DeleteStrategy deleteStrategy;

    private StrategyFactory strategyFactory;

    @BeforeEach
    void setUp() {
        strategyFactory = new StrategyFactory(insertStrategy, updateStrategy, deleteStrategy);
    }

    @Test
    @DisplayName("INSERT 전략 조회 성공")
    void testGetInsertStrategy() {
        // when
        CrudStrategy strategy = strategyFactory.getStrategy("INSERT");
        
        // then
        assertNotNull(strategy);
        assertEquals(insertStrategy, strategy);
    }

    @Test
    @DisplayName("UPDATE 전략 조회 성공")
    void testGetUpdateStrategy() {
        // when
        CrudStrategy strategy = strategyFactory.getStrategy("UPDATE");
        
        // then
        assertNotNull(strategy);
        assertEquals(updateStrategy, strategy);
    }

    @Test
    @DisplayName("DELETE 전략 조회 성공")
    void testGetDeleteStrategy() {
        // when
        CrudStrategy strategy = strategyFactory.getStrategy("DELETE");
        
        // then
        assertNotNull(strategy);
        assertEquals(deleteStrategy, strategy);
    }

    @Test
    @DisplayName("대소문자 구분 없이 전략 조회 성공")
    void testGetStrategyCaseInsensitive() {
        // when
        CrudStrategy strategy1 = strategyFactory.getStrategy("insert");
        CrudStrategy strategy2 = strategyFactory.getStrategy("INSERT");

        // then
        assertNotNull(strategy1);
        assertNotNull(strategy2);
        assertEquals(strategy1, strategy2);
    }

    @Test
    @DisplayName("지원하지 않는 작업 타입으로 전략 조회 시 예외 발생")
    void testGetStrategyWithUnsupportedOperation() {
        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> strategyFactory.getStrategy("UNSUPPORTED"));

        assertTrue(exception.getMessage().contains("Unsupported operation type"));
    }
}

