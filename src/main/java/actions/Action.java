package actions;

import data.Batch;

@FunctionalInterface
public interface Action {

    /**
     * @return input batch augmented with result per item
     */
    Batch process(Batch batch);
}
