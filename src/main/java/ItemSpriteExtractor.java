import net.runelite.cache.IndexType;
import net.runelite.cache.ItemManager;
import net.runelite.cache.SpriteManager;
import net.runelite.cache.TextureManager;
import net.runelite.cache.definitions.ItemDefinition;
import net.runelite.cache.definitions.ModelDefinition;
import net.runelite.cache.definitions.loaders.ModelLoader;
import net.runelite.cache.definitions.providers.ModelProvider;
import net.runelite.cache.fs.Archive;
import net.runelite.cache.fs.Index;
import net.runelite.cache.fs.Store;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ItemSpriteExtractor {

    public static void main(String[] args) {
        final String cachePath = args[0];
        final int width = Integer.parseInt(args[1]);
        final int height = Integer.parseInt(args[2]);
        final int startItemId = Integer.parseInt(args[3]);
        final int endItemId = Integer.parseInt(args[4]);

        File base = new File(cachePath);

        try (Store store = new Store(base)) {
            store.load();

            ItemManager itemManager = new ItemManager(store);
            itemManager.load();

            ModelProvider modelProvider = modelId -> {
                Index models = store.getIndex(IndexType.MODELS);
                Archive archive = models.getArchive(modelId);

                byte[] data = archive.decompress(store.getStorage().loadArchive(archive));
                ModelDefinition inventoryModel = new ModelLoader().load(modelId, data);
                return inventoryModel;
            };

            SpriteManager spriteManager = new SpriteManager(store);
            spriteManager.load();

            TextureManager textureManager = new TextureManager(store);
            textureManager.load();

            try {
                File outPath = new File("./sprite_dumps/" + width + "x" + height + "/");
                if (!outPath.exists()) {
                    outPath.mkdirs();
                }

                for (int i = startItemId; i <= endItemId; i++) {
                    BufferedImage sprite = ItemSpriteFactory.createSpritePixels(itemManager, modelProvider, spriteManager, textureManager,
                            i, 0, 1, 0, false, width, height, 1, 0x010101, 36D).toBufferedImage();

                    File out = new File(outPath.getAbsolutePath(), i + ".png");
                    BufferedImage img = sprite;
                    ImageIO.write(img, "PNG", out);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            for (ItemDefinition itemDef : itemManager.getItems()) {

                if (itemDef.id > 200) continue;

                if (itemDef.name == null || itemDef.name.equalsIgnoreCase("null")) {
                    continue;
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
