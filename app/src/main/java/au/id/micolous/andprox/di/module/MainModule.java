package au.id.micolous.andprox.di.module;

import javax.inject.Singleton;

import au.id.micolous.andprox.components.AboutAndProxFragment;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class MainModule {

    @Singleton
    @ContributesAndroidInjector(modules = AppModule.class)
    abstract AboutAndProxFragment contributeAboutFragment();
}
