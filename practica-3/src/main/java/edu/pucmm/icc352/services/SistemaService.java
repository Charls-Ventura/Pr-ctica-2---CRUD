package edu.pucmm.icc352.services;

public class SistemaService {

    public final AuthService auth;
    public final ProductoService productos;
    public final CarritoService carrito;
    public final CompraService compras;
    public final ComentarioService comentarios;
    public final UsuarioService usuarios;

    public SistemaService() {
        this.auth = new AuthService();
        this.productos = new ProductoService();
        this.carrito = new CarritoService();
        this.compras = new CompraService();
        this.comentarios = new ComentarioService();
        this.usuarios = new UsuarioService();
    }
}