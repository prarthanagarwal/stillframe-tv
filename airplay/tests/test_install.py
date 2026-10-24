import importlib.util
from pathlib import Path
import shlex
import tempfile
import unittest

spec = importlib.util.spec_from_file_location("airplay_install", Path(__file__).parents[1] / "install.py")
installer = importlib.util.module_from_spec(spec)
spec.loader.exec_module(installer)

class InstallerTests(unittest.TestCase):
    def test_verified_payload_and_tampering(self):
        with tempfile.TemporaryDirectory() as folder:
            path = Path(folder) / "payload"
            path.write_bytes(b"hello")
            known = "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824"
            installer.verify(path, known)
            path.write_bytes(b"changed")
            with self.assertRaises(ValueError): installer.verify(path, known)
    def test_receiver_names_remain_one_remote_argument(self):
        args = ["am", "broadcast", "--es", "value", "Living room's TV; $(echo example)"]
        self.assertEqual(args, shlex.split(installer.remote_command(args)))
    def test_pinned_release_uses_the_recorded_checksum(self):
        self.assertIn("/v0.0.33/", installer.URL)
        self.assertEqual(64, len(installer.SHA256))

if __name__ == "__main__": unittest.main()
