package io.lette1394.mediaserver.domain.storage.usecase;

import io.lette1394.mediaserver.domain.storage.Object;

interface Downloader {
    DataProvider download(Object object);
}
