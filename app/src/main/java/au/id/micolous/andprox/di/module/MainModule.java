package au.id.micolous.andprox.di.module;

import au.id.micolous.andprox.components.AboutAndProxFragment;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class MainModule {

    @ContributesAndroidInjector
    abstract AboutAndProxFragment contributeAboutFragment();
}
