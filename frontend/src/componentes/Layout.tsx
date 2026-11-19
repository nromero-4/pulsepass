import { Menu, Moon, Sun, Ticket, X } from 'lucide-react'
import clsx from 'clsx'
import { useEffect, useRef, useState } from 'react'
import { Link, Navigate, NavLink, Outlet, useLocation, useNavigate } from 'react-router'
import { useSesion } from '../features/auth/sesion'
import { RUTAS } from '../lib/rutas'
import { Boton, EnlaceBoton } from './ui'

const mediaTema = '(prefers-color-scheme: dark)'
function Tema() {
  const [manual, setManual] = useState<boolean>()
  const [sistema, setSistema] = useState(() => window.matchMedia?.(mediaTema).matches ?? false)
  const oscuro = manual ?? sistema
  useEffect(() => {
    const media = window.matchMedia?.(mediaTema)
    const actualizar = (event: MediaQueryListEvent) => setSistema(event.matches)
    media?.addEventListener('change', actualizar)
    return () => media?.removeEventListener('change', actualizar)
  }, [])
  useEffect(() => {
    document.documentElement.classList.toggle('dark', oscuro)
  }, [oscuro])
  const Icono = oscuro ? Sun : Moon
  return (
    <Boton
      variante="fantasma"
      className="px-3"
      aria-label={oscuro ? 'Activar modo claro' : 'Activar modo oscuro'}
      onClick={() => setManual(!oscuro)}
    >
      <Icono aria-hidden="true" className="size-5" />
    </Boton>
  )
}

const publicos: { to: string; label: string }[] = [
  { to: RUTAS.eventos, label: 'Eventos' },
  { to: RUTAS.artistas, label: 'Artistas' },
  { to: RUTAS.sedes, label: 'Sedes' },
]
function Navbar() {
  const { sesion, tieneRol, cerrarSesion } = useSesion()
  const location = useLocation()
  const navigate = useNavigate()
  const [menu, setMenu] = useState<string>()
  const [cuenta, setCuenta] = useState<string>()
  const boton = useRef<HTMLButtonElement>(null)
  const botonCuenta = useRef<HTMLButtonElement>(null)
  const abierto = menu === location.key
  const menuCuenta = cuenta === location.key
  const enlaces = [...publicos]
  if (tieneRol('USER')) enlaces.push({ to: RUTAS.tickets, label: 'Mis tickets' })
  if (tieneRol('STAFF', 'ADMIN'))
    enlaces.push(
      { to: RUTAS.validar, label: 'Validar ingreso' },
      { to: RUTAS.pagados, label: 'Tickets pagados' },
    )
  if (tieneRol('ADMIN')) enlaces.push({ to: RUTAS.admin, label: 'Administrar eventos' })

  function salir() {
    setCuenta(undefined)
    setMenu(undefined)
    void navigate(RUTAS.inicio, { replace: true, flushSync: true })
    cerrarSesion()
  }

  return (
    <header className="sticky top-0 z-30 border-b border-slate-200 bg-white/95 backdrop-blur dark:border-slate-800 dark:bg-slate-950/95">
      <div
        className="contenedor flex min-h-16 flex-wrap items-center justify-between gap-2"
        onKeyDown={(event) => {
          if (event.key !== 'Escape') return
          if (menuCuenta) {
            setCuenta(undefined)
            botonCuenta.current?.focus()
          } else if (abierto) {
            setMenu(undefined)
            boton.current?.focus()
          }
        }}
      >
        <NavLink
          to={RUTAS.inicio}
          end
          className="flex min-h-11 shrink-0 items-center gap-2 rounded-md font-titulo text-lg font-bold tracking-tight"
        >
          <span className="grid size-9 place-items-center rounded-xl bg-marca-600 text-white">
            <Ticket aria-hidden="true" className="size-5" />
          </span>
          PulsePass
        </NavLink>
        <div className="ml-auto flex items-center gap-1 lg:order-last">
          <Tema />
          <Boton
            ref={boton}
            variante="fantasma"
            className="px-3 md:hidden"
            aria-expanded={abierto}
            aria-controls="navegacion-principal"
            aria-label={abierto ? 'Cerrar menú' : 'Abrir menú'}
            onClick={() => setMenu(abierto ? undefined : location.key)}
          >
            {abierto ? (
              <X aria-hidden="true" className="size-5" />
            ) : (
              <Menu aria-hidden="true" className="size-5" />
            )}
          </Boton>
        </div>
        <nav
          id="navegacion-principal"
          aria-label="Principal"
          className={clsx(
            'w-full flex-col gap-2 pb-3 md:flex md:w-auto md:flex-1 md:flex-row md:flex-wrap md:items-center md:pb-0',
            abierto ? 'flex' : 'hidden',
          )}
        >
          <div className="flex flex-col gap-1 md:flex-row md:flex-wrap">
            {enlaces.map(({ to, label }) => (
              <NavLink
                key={to}
                to={to}
                onClick={() => setMenu(undefined)}
                className={({ isActive }) =>
                  clsx(
                    'flex min-h-11 items-center rounded-lg px-2.5 text-sm font-semibold hover:bg-slate-100 dark:hover:bg-slate-800',
                    isActive
                      ? 'bg-marca-50 text-marca-700 dark:bg-indigo-950 dark:text-indigo-200'
                      : 'text-slate-700 dark:text-slate-200',
                  )
                }
              >
                {label}
              </NavLink>
            ))}
          </div>
          {sesion.estado === 'anonimo' && (
            <div className="flex flex-wrap gap-2 border-t border-slate-200 pt-2 md:ml-auto md:border-0 md:pt-0 dark:border-slate-800">
              <EnlaceBoton to={RUTAS.login} variante="fantasma" onClick={() => setMenu(undefined)}>
                Ingresar
              </EnlaceBoton>
              <EnlaceBoton to={RUTAS.registro} onClick={() => setMenu(undefined)}>
                Registrarse
              </EnlaceBoton>
            </div>
          )}
          {sesion.estado === 'autenticado' && (
            <div className="relative md:ml-auto">
              <Boton
                ref={botonCuenta}
                variante="fantasma"
                aria-expanded={menuCuenta}
                aria-controls="menu-cuenta"
                aria-label="Menú de usuario"
                onClick={() => setCuenta(menuCuenta ? undefined : location.key)}
              >
                <span
                  aria-hidden="true"
                  className="grid size-7 place-items-center rounded-full bg-indigo-100 text-xs text-indigo-900"
                >
                  {sesion.usuario.username.slice(0, 2).toUpperCase()}
                </span>
                <span className="max-w-24 truncate">{sesion.usuario.username}</span>
              </Boton>
              <div
                id="menu-cuenta"
                hidden={!menuCuenta}
                className="superficie absolute top-full left-0 z-10 min-w-36 p-1 shadow-lg md:right-0 md:left-auto"
              >
                <EnlaceBoton
                  to={RUTAS.cuenta}
                  variante="fantasma"
                  className="w-full justify-start"
                  onClick={() => {
                    setMenu(undefined)
                    setCuenta(undefined)
                  }}
                >
                  Mi cuenta
                </EnlaceBoton>
                <Boton variante="fantasma" className="w-full justify-start" onClick={salir}>
                  Salir
                </Boton>
              </div>
            </div>
          )}
        </nav>
      </div>
    </header>
  )
}

export function Layout() {
  const { sesion, reconocerExpiracion } = useSesion()
  const { pathname, search, hash } = useLocation()
  const expirada = sesion.estado === 'anonimo' && sesion.motivo === 'expirada'
  useEffect(() => {
    if (expirada && pathname === RUTAS.login) reconocerExpiracion()
  }, [expirada, pathname, reconocerExpiracion])
  if (expirada && pathname !== RUTAS.login) {
    return (
      <Navigate
        to={RUTAS.login}
        replace
        state={{ desde: pathname + search + hash, motivo: 'expirada' }}
      />
    )
  }
  return (
    <div className="flex min-h-dvh flex-col">
      <a
        href="#contenido"
        className="sr-only focus:not-sr-only focus:fixed focus:top-2 focus:left-4 focus:z-50 focus:rounded-lg focus:bg-white focus:px-4 focus:py-3 focus:text-slate-900"
      >
        Saltar al contenido
      </a>
      <Navbar />
      <main
        id="contenido"
        tabIndex={-1}
        className={clsx('grow', pathname !== RUTAS.inicio && 'contenedor py-7 sm:py-10')}
      >
        <Outlet />
      </main>
      <footer className="border-t border-slate-200 py-5 dark:border-slate-800">
        <div className="contenedor secundario flex flex-wrap items-center justify-between gap-x-5 text-sm">
          <p>© 2026 PulsePass · Caso de estudio académico</p>
          <div className="flex gap-4">
            <Link to={RUTAS.sedes} className="enlace flex min-h-11 items-center">
              Sedes
            </Link>
            <Link to={RUTAS.artistas} className="enlace flex min-h-11 items-center">
              Artistas
            </Link>
          </div>
        </div>
      </footer>
    </div>
  )
}
