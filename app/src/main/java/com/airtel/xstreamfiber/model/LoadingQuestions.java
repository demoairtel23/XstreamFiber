package com.airtel.xstreamfiber.model;

public class LoadingQuestions {
    private static LoadingQuestions singleton = new LoadingQuestions( );

    /* A private Constructor prevents any other
     * class from instantiating.
     */
    private LoadingQuestions() {
    }

    /* Static 'instance' method */
    public static LoadingQuestions getInstance( ) {
        return singleton;
    }

    private String[] quesArr;

    public String[] getQuesArr() {
        return quesArr;
    }

    public void setQuesArr(String[] quesArr) {
        this.quesArr = quesArr;
    }
}
