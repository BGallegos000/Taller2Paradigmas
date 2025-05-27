package backend;

public interface TestEventListener {
    void onItemsLoaded();
    void onQuestionChanged(int newIndex);
    void onResultsReady();
}
