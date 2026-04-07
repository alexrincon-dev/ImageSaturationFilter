package dev.alexrincon.imagesaturationfilter.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.alexrincon.imagesaturationfilter.data.ImageProcessor
import dev.alexrincon.imagesaturationfilter.domain.ApplyFilterUseCase

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideImageProcessor(): ImageProcessor {
        return ImageProcessor()
    }

    @Provides
    fun provideApplyFilterUseCase(
        processor: ImageProcessor
    ): ApplyFilterUseCase {
        return ApplyFilterUseCase(processor)
    }
}
