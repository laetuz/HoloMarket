package id.neotica.holomarket.feature.category.presenter;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import id.neotica.holomarket.feature.category.contract.CategoryView;
import id.neotica.holomarket.feature.category.domain.CategoryItem;
import id.neotica.holomarket.network.ApiCallback;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class CategoryPresenterTest {

    @Mock Context mockContext;
    @Mock CategoryView mockView;

    private TestPresenter presenter;

    private static final String CATEGORY_JSON =
            "{\"slug\":\"apps\",\"name\":\"Apps\",\"children\":["
                    + "{\"slug\":\"games\",\"name\":\"Games\"},"
                    + "{\"slug\":\"tools\",\"name\":\"Tools\"}]}";

    private static class TestPresenter extends CategoryPresenter {
        ApiCallback categoryCallback;
        ApiCallback collectionCallback;
        int categoryCalls;
        int collectionCalls;
        String lastSlug;

        TestPresenter(Context context) {
            super(context);
        }

        @Override
        void requestCategory(String slug, ApiCallback callback) {
            categoryCalls++;
            lastSlug = slug;
            categoryCallback = callback;
        }

        @Override
        void requestCollection(String slug, ApiCallback callback) {
            collectionCalls++;
            collectionCallback = callback;
        }
    }

    @Before
    public void setUp() {
        presenter = new TestPresenter(mockContext);
        presenter.attach(mockView);
    }

    @Test
    public void load_requestsCategoryAndCollection() {
        presenter.load("apps");

        assertEquals(1, presenter.categoryCalls);
        assertEquals(1, presenter.collectionCalls);
        assertEquals("apps", presenter.lastSlug);
    }

    @Test
    public void loadCategory_success_rendersAllAndChildren() {
        presenter.load("apps");

        presenter.categoryCallback.onSuccess(CATEGORY_JSON);

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(mockView).renderSections(captor.capture());

        List<CategoryItem> items = captor.getValue();
        assertEquals(3, items.size());
        assertEquals("All", items.get(0).displayName);
        assertEquals("apps", items.get(0).slug);
        assertEquals("Games", items.get(1).displayName);
        assertEquals("games", items.get(1).slug);
        assertEquals("Tools", items.get(2).displayName);
    }

    @Test
    public void loadCategory_noChildren_rendersJustAll() {
        presenter.load("apps");

        presenter.categoryCallback.onSuccess("{\"slug\":\"apps\",\"name\":\"Apps\"}");

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(mockView).renderSections(captor.capture());
        assertEquals(1, captor.getValue().size());
    }

    @Test
    public void loadCategory_malformed_doesNotRender() {
        presenter.load("apps");

        presenter.categoryCallback.onSuccess("not json");

        verify(mockView, never()).renderSections(anyList());
    }

    @Test
    public void loadFeatured_success_renders() {
        presenter.load("apps");

        presenter.collectionCallback.onSuccess("{\"data\":[{\"package_name\":\"p\"}]}");

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(mockView).renderFeatured(captor.capture());
        assertEquals(1, captor.getValue().size());
    }

    @Test
    public void loadFeatured_empty_doesNotRender() {
        presenter.load("apps");

        presenter.collectionCallback.onSuccess("{\"data\":[]}");

        verify(mockView, never()).renderFeatured(anyList());
    }

    @Test
    public void detach_thenCallback_doesNotTouchView() {
        presenter.load("apps");

        presenter.detach();
        reset(mockView);

        presenter.categoryCallback.onSuccess(CATEGORY_JSON);
        presenter.collectionCallback.onSuccess("{\"data\":[{\"package_name\":\"p\"}]}");

        verifyNoMoreInteractions(mockView);
    }
}