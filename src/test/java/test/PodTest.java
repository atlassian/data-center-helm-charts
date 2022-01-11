package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.Product;
import static test.jackson.JsonNodeAssert.assertThat;

import java.util.Map;

class PodTest {
	private Helm helm;

	@BeforeEach
	void initHelm(TestInfo testInfo) {
		helm = new Helm(testInfo);
	}

	@ParameterizedTest
	@EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
	void podLabels(Product product) throws Exception {
		final var pname = product.name().toLowerCase();
		final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
				"podLabels.customlabel", "MY_LABEL_VAR",
				"podLabels.customlabel2", "my-label-var-2"
		));

		final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
		assertThat(statefulSet.getLabels().path("customlabel")).hasTextEqualTo("MY_LABEL_VAR");
		assertThat(statefulSet.getLabels().path("customlabel2")).hasTextEqualTo("my-label-var-2");
	}
}
