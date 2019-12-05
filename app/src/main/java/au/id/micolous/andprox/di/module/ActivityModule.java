package au.id.micolous.andprox.di.module;

import javax.inject.Singleton;

import au.id.micolous.andprox.activities.MainActivity;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ActivityModule {

    @ContributesAndroidInjector(modules = MainModule.class)
    abstract MainActivity contributeMainActivity();

}
