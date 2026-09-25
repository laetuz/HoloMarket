# Architecture Guide — MVP, Feature-First

A project-agnostic reference for structuring application code with **MVP** (Model–View–Presenter) and **feature-first packaging**. Drop this file into any project. It *dictates* the layering, dependency direction, and naming so every feature looks the same.

---

## 1. Purpose

- Keep **UI**, **presentation logic**, and **data access** separate.
- Make features **self-contained** and easy to find.
- Make presentation logic **unit-testable without a device/UI**.
- Give contributors one obvious place for every kind of code.

---

## 2. Core principles

1. **Separation of concerns** — a class does one job: render, coordinate, model, or fetch.
2. **Feature-first, then layer** — group by feature; inside a feature, split by layer.
3. **Dependency inversion** — inner layers define interfaces; outer layers implement them.
4. **Unidirectional flow** — events go in, rendered state comes out; no view reaching into data.
5. **Testability is a design constraint** — if it can't be unit-tested, the boundary is wrong.

---

## 3. Layers & responsibilities

| Layer | Package | Responsibility | May depend on |
|---|---|---|---|
| **View** | `ui/` | Render state, capture user input, navigation. No business logic. | presenter (via contract), domain (read-only models) |
| **Contract** | `contract/` | Interfaces the presenter talks to: the **View** interface (and optionally service/repository interfaces). | domain |
| **Presenter** | `presenter/` | Presentation logic + state. Translates events → data calls → view calls. | contract, domain |
| **Domain** | `domain/` | Models/entities, enums, value objects, use-cases, **repository interfaces**. Framework-agnostic. | nothing |
| **Data** *(optional)* | `data/` | Implements domain repository interfaces: network, DB, cache, files. | domain |

> `data/` is optional. The minimum viable MVP is `ui` + `contract` + `presenter` + `domain`. Add `data/` when you want to remove I/O from presenters and make them fully mockable.

---

## 4. Packaging convention

```
feature/<name>/
├── contract/
│   └── <Name>View.java
├── domain/
│   ├── <Entity>Model.java
│   └── <Thing>Repository.java     (interface, optional)
├── presenter/
│   └── <Name>Presenter.java
├── ui/
│   └── <Name>Activity.java        (+ adapters, view helpers)
└── data/                          (optional)
    └── Api<Thing>Repository.java  (implements the domain interface)
```

Rules:

- **One feature = one feature package.** `<name>` is singular and describes the user-facing capability (`auth`, `checkout`, `profile`, `appdetail`).
- **Multi-screen groups nest sub-features**, each with the full layout:
  `feature/auth/login/{contract,domain,presenter,ui}`, `feature/auth/register/…`.
- **Shared code does not live in a feature.** Put cross-feature UI in `common/ui`, shared models/utilities in `common/` or `core/`.
- **Feature-exclusive models live in that feature's `domain/`.** Only move a model to `common/` once a second feature needs it.

---

## 5. Dependency rules (MUST)

- **Inward only:** `ui → presenter → domain`, `data → domain`, `ui → domain` (models only).
- **`domain` depends on nothing** — no UI framework, no network client, no platform types.
- **View contracts MUST NOT import** concrete views or presenters.
- **Presenters MUST NOT import** UI classes; they only call the **contract** interface.
- **Views MUST NOT** call data/network directly.
- **No cyclic dependencies** between features. If two features need each other, extract the shared piece to `common/`.

---

## 6. Feature anatomy

Generic example — a "profile" feature with a repository:

```
feature/profile/
├── contract/
│   └── ProfileView.java            # refresh(), showName(String), showError(String)
├── domain/
│   ├── ProfileModel.java           # data model
│   └── ProfileRepository.java      # loadProfile(callback)
├── data/
│   └── ApiProfileRepository.java   # implements ProfileRepository
├── presenter/
│   └── ProfilePresenter.java       # attach/detach/load; calls repository; drives ProfileView
└── ui/
    └── ProfileActivity.java        # implements ProfileView
```

---

## 7. Data flow (unidirectional)

```
user event ──► View ──► Presenter.method()
                          │
                          ▼
                  domain / data (via interface)
                          │
                          ▼
                  Presenter callback
                          │
                          ▼
             View.render...()  ──► UI updates
```

- The View never mutates state directly based on a response; it only renders what the Presenter tells it.
- The Presenter owns state; the View is stateless where possible.

---

## 8. Presenter lifecycle & threading

- Provide `attach(view)` / `detach()`; the View calls `detach()` in `onDestroy`.
- **Guard every async callback** with `if (view != null)` so a detached view is never touched.
- Keep long/async work off the main thread; deliver results back on the main thread before touching the view.
- Prefer one presenter per screen/flow. Split when a screen has genuinely independent regions.

---

## 9. Naming conventions

| Kind | Convention | Example |
|---|---|---|
| View contract (interface) | `<Screen>View` | `ProfileView` |
| Presenter | `<Screen>Presenter` | `ProfilePresenter` |
| Activity / UI screen | `<Screen>Activity` | `ProfileActivity` |
| Model / entity | `<Entity>Model` | `ProfileModel` |
| Repository interface | `<Entity>Repository` | `ProfileRepository` |
| Repository impl | `<Source><Entity>Repository` | `ApiProfileRepository` |
| Use-case (optional) | Verb + noun | `LoadProfile` |

---

## 10. Testing strategy

- **Mirror the main packages** under the test source set: `test/.../feature/profile/presenter/ProfilePresenterTest`.
- **Unit-test presenters** with a mocked View contract and a fake/injected repository (or stubbed I/O seam). No device, no real network.
- **Unit-test domain models** (parsing, value objects, validation) directly.
- Whether you inject a repository or expose a package-private I/O **seam**, the rule is: *the presenter must be exercisable without the real data source*.
  - **Preferred:** constructor-inject a `domain` repository interface; tests pass a fake.
  - **Pragmatic (small apps):** keep the I/O call in an overridable method and let tests substitute it.

---

## 11. Adopting incrementally (migrating legacy code)

1. Pick the **smallest, most self-contained feature** first.
2. Create `feature/<name>/{contract,domain,presenter,ui}`; move the screen.
3. Extract a `<Name>View` interface; make the Activity implement it and delegate to a new `<Name>Presenter`.
4. Move network/parsing/state into the presenter; leave only rendering/navigation in the Activity.
5. **Update fully-qualified names** (manifests/registries/route tables and every reference) — moving a class changes its FQN.
6. Add a `<Name>PresenterTest`; then move on to the next feature.
7. Track progress in a checklist until no legacy feature remains.

---

## 12. Anti-patterns to avoid

- **God Activity/Screen** — hundreds of lines mixing UI, network, parsing, and state.
- **Android/platform types leaking into presenters** — presenters should not need view classes.
- **View contracts that import implementations** — contracts depend on nothing but domain.
- **Business logic in the view** — validation, comparisons, and decisions belong in the presenter.
- **Cyclic feature dependencies** — extract shared code instead.
- **Untestable presenters** — if you can't run it in a unit test, the boundary is wrong.

---

## Appendix — short Android/Java example

A minimal feature demonstrating the boundaries (framework-agnostic in spirit; Java shown).

```java
// domain/ProfileModel.java  (framework-agnostic)
public class ProfileModel {
    public final String name;
    public ProfileModel(String name) { this.name = name; }
}

// domain/ProfileRepository.java  (interface owns the contract)
public interface ProfileRepository {
    void loadProfile(Callback<ProfileModel> callback);
}

// contract/ProfileView.java  (view contract — depends on domain only)
public interface ProfileView {
    void renderProfile(String name);
    void showError(String message);
}

// presenter/ProfilePresenter.java  (owns logic + state; depends on the interface)
public class ProfilePresenter {
    private final ProfileRepository repository;
    private ProfileView view;

    public ProfilePresenter(ProfileRepository repository) { this.repository = repository; }
    public void attach(ProfileView view) { this.view = view; }
    public void detach() { this.view = null; }

    public void load() {
        repository.loadProfile(new Callback<ProfileModel>() {
            @Override public void onResult(ProfileModel profile) {
                if (view == null) return;          // guard detached view
                view.renderProfile(profile.name);
            }
            @Override public void onError(String message) {
                if (view == null) return;
                view.showError(message);
            }
        });
    }
}

// ui/ProfileActivity.java  (renders only)
public class ProfileActivity extends Activity implements ProfileView {
    private ProfilePresenter presenter;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_profile);
        presenter = new ProfilePresenter(new ApiProfileRepository(this));
        presenter.attach(this);
        presenter.load();
    }

    @Override public void renderProfile(String name) { /* update TextView */ }
    @Override public void showError(String message)  { /* show error UI */ }
    @Override protected void onDestroy() { presenter.detach(); super.onDestroy(); }
}
```

A test needs no UI framework:

```java
// test/.../feature/profile/presenter/ProfilePresenterTest.java
ProfileView view = mock(ProfileView.class);
ProfileRepository repo = callback -> callback.onResult(new ProfileModel("Ada"));
new ProfilePresenter(repo).load();
verify(view).renderProfile("Ada");
```