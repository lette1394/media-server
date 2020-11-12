package io.lette1394.mediaserver.suite;

import static io.lette1394.mediaserver.suite.TestSuiteTag.SLOW;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.ExcludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
@SelectPackages("io.lette1394.mediaserver")
@ExcludeTags(SLOW)
public class ExcludeSlowTestSuite {
}
