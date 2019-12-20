package au.id.micolous.andprox.di.component;

import javax.inject.Singleton;

import au.id.micolous.andprox.activities.AppCompatPreferenceActivity;
import au.id.micolous.andprox.activities.InjectableActivity;
import au.id.micolous.andprox.components.InjectableFragment;
import au.id.micolous.andprox.di.module.AppModule;
import dagger.Component;

@Singleton
@Component(modules = {
        AppModule.class
})
public interface AppComponent {

    void inject(InjectableFragment fragment);

    void inject(InjectableActivity activity);

    void inject(AppCompatPreferenceActivity preferenceActivity);
}
