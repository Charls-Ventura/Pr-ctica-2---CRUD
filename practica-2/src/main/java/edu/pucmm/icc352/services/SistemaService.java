package edu.pucmm.icc352.services;

public class SistemaService {
    public final AuthService auth = new AuthService();
    public final ProductoService productos = new ProductoService();
    public final CarritoService carrito = new CarritoService();
    public final CompraService compras = new CompraService();
}
