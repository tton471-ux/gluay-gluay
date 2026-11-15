import unittest

from backend.app.services.effects import EFFECT_FILTERS


class EffectsCatalogueTests(unittest.TestCase):
    def test_expected_effects_are_available(self):
        expected = {
            "none", "cinematic", "vintage", "bw", "warm", "cool",
            "vivid", "soft", "sharpen", "dream", "mirror", "flip",
            "blur", "edge", "pixel", "night", "sunset", "oldtv",
        }
        self.assertTrue(expected.issubset(EFFECT_FILTERS.keys()))

    def test_only_original_has_no_filter(self):
        self.assertIsNone(EFFECT_FILTERS["none"])
        self.assertTrue(all(value for key, value in EFFECT_FILTERS.items() if key != "none"))


if __name__ == "__main__":
    unittest.main()
