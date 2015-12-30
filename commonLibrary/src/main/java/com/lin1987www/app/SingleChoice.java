package com.lin1987www.app;

public abstract class SingleChoice<T> extends Choice<T> {
    public SingleChoice() {
        setChoiceMode(CHOICE_MODE_SINGLE);
    }
}
