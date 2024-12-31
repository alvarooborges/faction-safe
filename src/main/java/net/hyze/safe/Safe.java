package net.hyze.safe;

import lombok.*;
import org.bukkit.Material;

@Getter
@EqualsAndHashCode(of = "id")
@RequiredArgsConstructor
@AllArgsConstructor
public class Safe {

    private final int id;

    @Setter
    private Material iconMaterial;

    @Setter
    private int materialData;
}
