package co.edu.uceva.buildcheck.modules.materiales.model;

public enum UnidadMedida {
    METRO_LINEAL, // Para tubería, cableado, guardescobas (ML)
    METRO_CUADRADO, // Para piso, pintura, pañete, drywall (M2)
    METRO_CUBICO, // FUNDAMENTAL: Para arena, triturado, concreto, tierra (M3)
    KILOGRAMO, // Para clavos, amarres, aditivos en polvo (KG)
    BULTO, // Cemento, cal, pegante blanco (BLT)
    LITRO, // Pinturas pequeñas, impermeabilizantes líquidos (L)
    GALON, // Pinturas, anticorrosivos (GL)
    UNIDAD, // Ladrillos, bloques, tejas, puertas, sanitarios (UND)
    GLOBAL// Para servicios o contratos a "todo costo" (GLB)
}
